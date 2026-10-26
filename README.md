# VS Code cho Android (full-stack)

App Android chạy VS Code thật qua code-server trên VPS: terminal thật, Node/Python, extensions, Git.
Không có server thì vẫn dùng được chế độ offline (Monaco editor).

## Chế độ full-stack (khuyên dùng)

1. SSH vào VPS, chạy code-server:
```bash
curl -fsSL https://code-server.dev/install.sh | sh
code-server --host 0.0.0.0 --port 8080
```
2. Mở app > nhập `Server URL` + `Password` > Kết nối.
3. Dùng VS Code đầy đủ: terminal, chạy backend, cài extension.

## Cách chạy nhanh (không cần Android Studio)

1. Mở `www/index.html` bằng Chrome trên điện thoại là dùng luôn.
2. Hoặc chạy local:
```bash
cd www
npx serve .
```

## Cách lấy file APK (không cần cài SDK)

1. Push code này lên GitHub repo.
2. Vào tab `Actions` -> workflow `Build APK` -> tải file `vscode-android-apk` ở mục Artifacts.
3. Cài APK lên máy Android.

## Cách build local (nếu có Android Studio)

```bash
# Mở thư mục android/ bằng Android Studio rồi bấm Run
```

## Tính năng

- Monaco Editor (đúng editor của VS Code): highlight, IntelliSense cơ bản, multi-tab
- File explorer: tạo / xóa / đổi tên file & folder, lưu vào localStorage + Android file system
- Preview HTML, chạy JavaScript
- Tìm kiếm trong file, terminal giả lập, dark theme giống VS Code
- Mở file thật trên Android qua Storage Access Framework
