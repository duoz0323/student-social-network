from __future__ import annotations

import json

from app.inference import PhoBertHsdEngine
from app.settings import Settings


def main() -> None:
    """Smoke test thật; chỉ in label/scores, không in lại nội dung đầu vào."""
    engine = PhoBertHsdEngine.load(Settings.from_environment())
    samples = [
        "Hôm nay lớp mình học rất vui.",
        "Đồ ngu, nói chuyện chán thật.",
        "Tao ghét và muốn tiêu diệt hết bọn đó.",
    ]
    for index, sample in enumerate(samples, start=1):
        result = engine.moderate(sample)
        print(json.dumps({"sample": index, **result.model_dump()}, ensure_ascii=False))


if __name__ == "__main__":
    main()
