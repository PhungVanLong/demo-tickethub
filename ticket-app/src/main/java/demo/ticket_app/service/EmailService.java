package demo.ticket_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mailtrap.from.email}")
    private String fromEmail;

    @Value("${mailtrap.from.name}")
    private String fromName;

    public void sendOtpEmail(String to, String otp) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("🔐 Mã OTP đặt lại mật khẩu TicketHub");
        helper.setFrom(fromEmail, fromName);
        helper.setText(buildOtpHtml(otp), true); // true = gửi HTML
        mailSender.send(mimeMessage);
    }

    private String buildOtpHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Mã OTP TicketHub</title>
            </head>
            <body style="margin:0;padding:0;background-color:#0f0f1a;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0f0f1a;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:linear-gradient(135deg,#1a1a2e 0%%,#16213e 100%%);border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.5);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(90deg,#7c3aed,#a855f7);padding:32px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:700;letter-spacing:1px;">🎫 TicketHub</h1>
                          <p style="margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;">Hệ thống đặt vé sự kiện</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#e2e8f0;font-size:20px;font-weight:600;">Đặt lại mật khẩu</h2>
                          <p style="margin:0 0 28px;color:#94a3b8;font-size:15px;line-height:1.6;">
                            Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Sử dụng mã OTP bên dưới để tiếp tục:
                          </p>
                          <!-- OTP Box -->
                          <div style="background:linear-gradient(135deg,#7c3aed20,#a855f720);border:1px solid #7c3aed50;border-radius:12px;padding:28px;text-align:center;margin:0 0 28px;">
                            <p style="margin:0 0 8px;color:#a78bfa;font-size:13px;font-weight:500;letter-spacing:2px;text-transform:uppercase;">Mã OTP của bạn</p>
                            <div style="font-size:42px;font-weight:700;letter-spacing:10px;color:#f8fafc;font-family:monospace;">%s</div>
                            <p style="margin:12px 0 0;color:#64748b;font-size:13px;">⏱ Mã có hiệu lực trong <strong style="color:#a78bfa;">5 phút</strong></p>
                          </div>
                          <p style="margin:0 0 16px;color:#64748b;font-size:13px;line-height:1.6;border-left:3px solid #7c3aed;padding-left:12px;">
                            Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. Mã sẽ tự động hết hạn sau 5 phút.
                          </p>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="background:#0a0a14;padding:20px 40px;text-align:center;border-top:1px solid #1e293b;">
                          <p style="margin:0;color:#475569;font-size:12px;">© 2026 TicketHub. Mọi quyền được bảo lưu.</p>
                          <p style="margin:6px 0 0;color:#475569;font-size:12px;">Email này được gửi tự động, vui lòng không trả lời.</p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(otp);
    }
}