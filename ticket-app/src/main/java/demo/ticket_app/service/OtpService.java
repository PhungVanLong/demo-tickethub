package demo.ticket_app.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final long OTP_EXPIRY_SECONDS = 5 * 60; // 5 phút

    private record OtpEntry(String otp, Instant expiresAt) {}

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpStorage.put(email, new OtpEntry(otp, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS)));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStorage.get(email);
        if (entry == null) return false;

        // Kiểm tra hết hạn
        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStorage.remove(email);
            return false;
        }

        if (entry.otp().equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }
}