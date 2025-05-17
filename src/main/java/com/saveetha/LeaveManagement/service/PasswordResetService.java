package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.PasswordResetToken;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import com.saveetha.LeaveManagement.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final EmployeeRepository employeeRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // ✅ Add encoder

    public PasswordResetService(EmployeeRepository employeeRepository, PasswordResetTokenRepository tokenRepository) {
        this.employeeRepository = employeeRepository;
        this.tokenRepository = tokenRepository;
    }

    public Optional<String> createPasswordResetToken(String email) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isEmpty()) {
            return Optional.empty();
        }

        Employee employee = employeeOpt.get();

        // Generate token
        String token = UUID.randomUUID().toString();

        // Save token with expiry (e.g., 1 hour later)
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmployee(employee);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        tokenRepository.save(resetToken);

        return Optional.of(token);
    }

    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        if (resetTokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        if (resetTokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        Employee employee = resetToken.getEmployee();
        // ✅ Encode password before saving
        employee.setPassword(passwordEncoder.encode(newPassword));

        employeeRepository.save(employee);

        // Optionally delete the token after use
        tokenRepository.delete(resetToken);

        return true;
    }
}
