package chainsawman.gesture.consumer;

import chainsawman.gesture.dto.meeting.request.MeetingMinutesCreateRequest;
import chainsawman.gesture.service.MeetingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>>, InitializingBean, DisposableBean {

    private final RedisConnectionFactory redisConnectionFactory;
    private final StringRedisTemplate stringRedisTemplate;
    private final MeetingService meetingService;
    private final ObjectMapper objectMapper;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private Subscription subscription;

    private final String streamKey = "meeting-stream";
    private final String consumerGroupName = "core-group";
    private final String consumerName = "core-consumer-1";

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Redis Stream Consumer 초기화 시작...");
        prepareStreamAndGroup();

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        this.listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);

        Consumer consumer = Consumer.from(consumerGroupName, consumerName);
        StreamOffset<String> streamOffset = StreamOffset.create(streamKey, ReadOffset.lastConsumed());

        this.subscription = this.listenerContainer.receive(consumer, streamOffset, this);
        this.listenerContainer.start();
        log.info("명세서 기반 회의록 수집 Redis Stream 컨슈머 가동 완료");
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> body = message.getValue();
            log.info("Redis Stream 회의록 데이터 수신 [ID: {}]", message.getId());

            // 필수 파라미터 추출
            Long callIdx = Long.parseLong(body.get("callIdx"));

            // json 파싱
            List<String> transcript = null;
            if (body.get("transcript") != null) {
                transcript = objectMapper.readValue(body.get("transcript"), new TypeReference<List<String>>(){});
            }

            List<String> participants = null;
            if (body.get("participants") != null) {
                participants = objectMapper.readValue(body.get("participants"), new TypeReference<List<String>>(){});
            }

            List<String> conclusion = null;
            if (body.get("conclusion") != null) {
                conclusion = objectMapper.readValue(body.get("conclusion"), new TypeReference<List<String>>(){});
            }

            // dto 바인딩
            MeetingMinutesCreateRequest request = MeetingMinutesCreateRequest.builder()
                    .title(body.get("title"))
                    .transcript(transcript)
                    .participants(participants)
                    .aiSummary(body.get("aiSummary"))
                    .conclusion(conclusion)
                    .build();

            meetingService.createMinutes(callIdx, request);

            // DB 트랜잭션이 완전히 성공했을 때만 수동 ACK 전송 (장애 방어를 위함)
            stringRedisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, message.getId());
            log.info("회의록 적재 및 Redis ACK 처리 완료 [ID: {}]", message.getId());

        } catch (Exception e) {
            // ACK를 보내지 않으므로 에러 발생 시 데이터가 보존
            log.error("회의록 스트림 처리 중 예외 발생 (ACK 미전송 / 보존 처리): ", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("Redis Stream Consumer 안전 종료 중...");
        if (this.subscription != null) this.subscription.cancel();
        if (this.listenerContainer != null) this.listenerContainer.stop();
    }

    private void prepareStreamAndGroup() {
        try {
            redisConnectionFactory.getConnection().streamCommands()
                    .xGroupCreate(
                            streamKey.getBytes(),
                            consumerGroupName,
                            ReadOffset.latest(),
                            true
                    );
        } catch (Exception e) {
            log.debug("소비자 그룹이 이미 존재하여 생성을 건너뜁니다.");
        }
    }
}