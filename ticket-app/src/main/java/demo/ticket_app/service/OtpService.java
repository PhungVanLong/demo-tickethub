package demo.ticket_app.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final long OTP_EXPIRY_MINUTES = 5;
    private static final String OTP_KEY_PREFIX = "otp:";

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom random = new SecureRandom();

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String key = OTP_KEY_PREFIX + email;
        // Lưu OTP vào Redis với TTL 5 phút
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(otp)) {
            // Xóa OTP sau khi dùng (one-time use)
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}