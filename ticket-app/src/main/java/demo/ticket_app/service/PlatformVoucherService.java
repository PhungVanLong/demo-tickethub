package demo.ticket_app.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import demo.ticket_app.entity.PlatformVoucher;
import demo.ticket_app.repository.PlatformVoucherRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlatformVoucherService {
    private final PlatformVoucherRepository platformVoucherRepository;

    public PlatformVoucher create(PlatformVoucher voucher) {
        return platformVoucherRepository.save(voucher);
    }

    public Optional<PlatformVoucher> findByCode(String code) {
        return platformVoucherRepository.findByCode(code);
    }

    // Thêm các hàm update, delete nếu cần
}
