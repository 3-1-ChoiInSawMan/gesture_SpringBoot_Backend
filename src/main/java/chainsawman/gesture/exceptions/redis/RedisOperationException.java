package chainsawman.gesture.exceptions.redis;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class RedisOperationException extends DomainException {
    public RedisOperationException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_STREAM_001", "Redis 작업 중 오류가 발생했습니다.");
    }
}

