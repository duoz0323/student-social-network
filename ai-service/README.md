# Local AI Content Moderation Service

FastAPI service chạy checkpoint `visolex/phobert-v2-hsd` cục bộ cho luồng moderation text tiếng Việt.
Service không gửi nội dung người dùng tới dịch vụ AI tính phí bên ngoài.

## Phạm vi model

- Ba label checkpoint: `CLEAN`, `OFFENSIVE`, `HATE`.
- Phạm vi phù hợp: phát hiện nội dung sạch, xúc phạm và thù ghét trong text tiếng Việt.
- Không phải bộ kiểm duyệt an toàn tổng quát cho ảnh/video, tự hại, tình dục, spam, thông tin sai lệch hoặc mọi ngôn ngữ.
- Context huấn luyện là 256 token. Service chia input theo token thành các đoạn 254 payload token + 2 special token và chọn mức nghiêm trọng nhất, vì vậy không cắt bỏ phần đuôi.
- Checkpoint card hướng dẫn tokenize raw text trực tiếp; service không thêm VnCoreNLP/underthesea khi chưa có bằng chứng checkpoint đã huấn luyện với bước đó.

## Nguồn model và giấy phép

- Checkpoint: [`visolex/phobert-v2-hsd`](https://huggingface.co/visolex/phobert-v2-hsd), pin revision `221aca47de6568d519eba61a94d7fdae3ca680ae`, model card khai báo MIT.
- Base model: [`vinai/phobert-base-v2`](https://huggingface.co/vinai/phobert-base-v2), pin revision `86cd7fd4c148980922ac11a2cf5e257f2ba639e1`, giấy phép AGPL-3.0.
- Khai báo MIT của checkpoint không loại bỏ nghĩa vụ từ base model AGPL-3.0; cần review tuân thủ AGPL trước khi phân phối/deploy sản phẩm.
- Khi phát hành hoặc dùng trong kết quả nghiên cứu, giữ attribution và citation PhoBERT theo model card của VinAI.
- Checkpoint chứa custom `models.py`. Runtime cố ý không thực thi remote code; nó dựng đúng kiến trúc `encoder + dropout + classifier`, nạp trực tiếp `model.safetensors` và từ chối startup nếu state-dict không khớp.

## Cài đặt Windows PowerShell

```powershell
cd ai-service
py -3.12 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements-dev.txt
python -m pytest
python -m scripts.smoke_real_model
python -m uvicorn app.main:app --host 127.0.0.1 --port 8001
```

Nếu máy không có lệnh `py`, dùng đường dẫn tới Python 3.11/3.12 đã cài. Lần đầu service tải khoảng 540 MB trọng số và các file tokenizer/config; các lần sau dùng cache.

## Cài đặt Linux/macOS

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements-dev.txt
python -m pytest
python -m scripts.smoke_real_model
python -m uvicorn app.main:app --host 127.0.0.1 --port 8001
```

## Cấu hình

Sao chép `.env.example` vào cấu hình Run hoặc export các biến cần thay đổi. Mặc định:

- `AI_DEVICE=auto`: dùng CUDA nếu PyTorch phát hiện GPU, nếu không dùng CPU.
- `AI_MODEL_CACHE_DIR=.cache/huggingface`: cache model trong `ai-service` và đã được gitignore.
- `AI_MODEL_MAX_LENGTH=256`: không được tăng vượt context thực của PhoBERT.
- `AI_MAX_INPUT_CHARACTERS=20000`: giới hạn bảo vệ API; nghiệp vụ Post/Comment vẫn có giới hạn ngắn hơn tại Spring.

Sau khi cache đã đủ, service có thể khởi động offline. Không xóa cache nếu muốn tránh tải lại. Trên Windows chưa bật Developer Mode, Hugging Face có thể cảnh báo không dùng được symlink; inference vẫn hoạt động nhưng cache tốn thêm dung lượng.

## API

```http
GET /health
GET /ready
POST /v1/moderation
Content-Type: application/json

{"text":"Nội dung tiếng Việt cần kiểm tra"}
```

Response:

```json
{
  "label": "CLEAN",
  "confidence": 0.98,
  "scores": {"CLEAN": 0.98, "OFFENSIVE": 0.01, "HATE": 0.01}
}
```

`/health` chỉ xác nhận process sống. `/ready` chỉ trả 200 sau khi model nạp thành công. Khi model chưa sẵn sàng hoặc inference lỗi, moderation trả 503 để Spring áp dụng fail-closed.

Service không log raw text, token IDs hoặc raw response inference. CPU phù hợp development/đồ án; production cần benchmark latency, giới hạn concurrency và cân nhắc worker/GPU theo tài nguyên thực tế.
