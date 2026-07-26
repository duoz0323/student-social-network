import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

function normalizeMedia(post) {
  if (Array.isArray(post.media) && post.media.length > 0) {
    return [...post.media]
      .sort((first, second) => (first.displayOrder ?? 0) - (second.displayOrder ?? 0))
      .slice(0, 4);
  }

  // Hỗ trợ dữ liệu màn hình cũ chỉ có danh sách URL ảnh.
  return (post.imageUrls ?? []).slice(0, 4).map((url, index) => ({
    id: `${post.id}-image-${index}`,
    url,
    mediaType: 'IMAGE',
    displayOrder: index,
  }));
}

function CloseIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M18 6 6 18" />
      <path d="m6 6 12 12" />
    </svg>
  );
}

function ArrowIcon({ direction }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
      <path d={direction === 'previous' ? 'm15 18-6-6 6-6' : 'm9 18 6-6-6-6'} />
    </svg>
  );
}

function ImageLightbox({ images, selectedIndex, onChange, onClose }) {
  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape') onClose();
      if (event.key === 'ArrowLeft' && selectedIndex > 0) onChange(selectedIndex - 1);
      if (event.key === 'ArrowRight' && selectedIndex < images.length - 1) onChange(selectedIndex + 1);
    }

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleKeyDown);
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [images.length, onChange, onClose, selectedIndex]);

  const selectedImage = images[selectedIndex];

  return createPortal(
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-black/95 p-3 sm:p-8"
      role="dialog"
      aria-modal="true"
      aria-label="Xem ảnh bài viết"
      onClick={onClose}
    >
      <button
        className="absolute right-4 top-4 z-10 flex h-11 w-11 items-center justify-center rounded-full bg-white/10 text-white transition hover:bg-white/20"
        onClick={onClose}
        aria-label="Đóng ảnh"
      >
        <CloseIcon />
      </button>

      {selectedIndex > 0 && (
        <button
          className="absolute left-3 z-10 flex h-11 w-11 items-center justify-center rounded-full bg-black/50 text-white transition hover:bg-black/70 sm:left-6"
          onClick={(event) => {
            event.stopPropagation();
            onChange(selectedIndex - 1);
          }}
          aria-label="Ảnh trước"
        >
          <ArrowIcon direction="previous" />
        </button>
      )}

      <img
        className="max-h-full max-w-full select-none object-contain"
        src={selectedImage.url}
        alt={`Ảnh đính kèm ${selectedIndex + 1}`}
        onClick={(event) => event.stopPropagation()}
      />

      {selectedIndex < images.length - 1 && (
        <button
          className="absolute right-3 z-10 flex h-11 w-11 items-center justify-center rounded-full bg-black/50 text-white transition hover:bg-black/70 sm:right-6"
          onClick={(event) => {
            event.stopPropagation();
            onChange(selectedIndex + 1);
          }}
          aria-label="Ảnh tiếp theo"
        >
          <ArrowIcon direction="next" />
        </button>
      )}

      {images.length > 1 && (
        <span className="absolute bottom-4 rounded-full bg-black/60 px-3 py-1.5 text-sm font-medium text-white">
          {selectedIndex + 1}/{images.length}
        </span>
      )}
    </div>,
    document.body,
  );
}

function mediaLayout(item, multiple) {
  const width = Number(item.width);
  const height = Number(item.height);
  const ratio = width > 0 && height > 0 ? width / height : 4 / 3;

  if (ratio < 0.8) {
    return multiple
      ? 'aspect-[3/4] w-[58%] shrink-0 snap-start sm:w-[52%]'
      : 'aspect-[3/4] w-[76%] max-w-[420px]';
  }
  if (ratio <= 1.2) {
    return multiple
      ? 'aspect-square w-[76%] shrink-0 snap-start sm:w-[68%]'
      : 'aspect-square w-full max-w-[520px]';
  }
  if (ratio <= 1.8) {
    return multiple
      ? 'aspect-[4/3] w-[86%] shrink-0 snap-start sm:w-[78%]'
      : 'aspect-[4/3] w-full';
  }
  return multiple
    ? 'aspect-video w-[88%] shrink-0 snap-start sm:w-[84%]'
    : 'aspect-video w-full';
}

function MediaItem({ item, index, multiple, onOpenImage }) {
  const isVideo = item.mediaType === 'VIDEO';
  const layoutClass = mediaLayout(item, multiple);

  return (
    <div className={`relative max-h-[520px] overflow-hidden rounded-[14px] border border-[var(--app-border)] bg-zinc-100 ${layoutClass}`}>
      {isVideo ? (
        <video
          className="h-full w-full object-cover"
          src={item.url}
          poster={item.thumbnailUrl || undefined}
          controls
          playsInline
          preload="metadata"
          aria-label={`Video đính kèm ${index + 1}`}
        />
      ) : (
        <button
          type="button"
          className="block h-full w-full cursor-zoom-in"
          onClick={onOpenImage}
          aria-label={`Mở rộng ảnh đính kèm ${index + 1}`}
        >
          <img
            className="h-full w-full object-cover"
            src={item.url}
            alt={`Ảnh đính kèm ${index + 1}`}
            loading="lazy"
          />
        </button>
      )}
    </div>
  );
}

export default function PostMediaGrid({ post }) {
  const media = normalizeMedia(post);
  const images = media.filter((item) => item.mediaType !== 'VIDEO');
  const [selectedImageIndex, setSelectedImageIndex] = useState(null);

  if (media.length === 0) return null;

  return (
    <>
      <div
        className="mt-3 flex snap-x snap-mandatory gap-2 overflow-x-auto overscroll-x-contain pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        aria-label={`${media.length} phương tiện đính kèm`}
      >
        {media.map((item, index) => {
          const imageIndex = item.mediaType === 'VIDEO' ? -1 : images.indexOf(item);
          return (
            <MediaItem
              key={item.id ?? `${item.url}-${index}`}
              item={item}
              index={index}
              multiple={media.length > 1}
              onOpenImage={() => setSelectedImageIndex(imageIndex)}
            />
          );
        })}
      </div>

      {selectedImageIndex !== null && (
        <ImageLightbox
          images={images}
          selectedIndex={selectedImageIndex}
          onChange={setSelectedImageIndex}
          onClose={() => setSelectedImageIndex(null)}
        />
      )}
    </>
  );
}
