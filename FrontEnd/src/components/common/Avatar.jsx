export default function Avatar({ src, name, size = 'md' }) {
  const sizes = { sm: 'h-9 w-9 text-sm', md: 'h-11 w-11 text-base', lg: 'h-20 w-20 text-2xl' };
  const initials = name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  return (
    <div className={`${sizes[size]} overflow-hidden rounded-full bg-violet-100 text-violet-700`}>
      {src ? (
        <img src={src} alt={name} className="h-full w-full object-cover" onError={(event) => (event.currentTarget.style.display = 'none')} />
      ) : (
        <span className="flex h-full w-full items-center justify-center font-bold">{initials}</span>
      )}
    </div>
  );
}
