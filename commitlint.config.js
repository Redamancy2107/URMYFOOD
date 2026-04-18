module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'feat',     // Tính năng mới
        'fix',      // Sửa lỗi
        'docs',     // Tài liệu
        'style',    // Cấu trúc code, không ảnh hưởng logic (css, formatting...)
        'refactor', // Cấu trúc lại code
        'perf',     // Tối ưu hiệu năng
        'test',     // Viết test
        'build',    // Thay đổi hệ thống build (gradle, npm...)
        'ci',       // Thay đổi cấu hình CI/CD (GitHub Actions...)
        'chore',    // Các thay đổi nhỏ khác
        'revert'    // Quay lại version cũ
      ]
    ],
    'header-max-length': [2, 'always', 100]        // Giới hạn 100 ký tự cho header
  }
};
