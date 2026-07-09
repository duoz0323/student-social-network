import { useEffect } from 'react';

export default function useKeyboardShortcut(key, callback, ctrlKey = false) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      // Ignore shortcuts if the user is typing in an input, textarea, etc.
      if (['INPUT', 'TEXTAREA', 'SELECT'].includes(event.target.tagName)) {
        return;
      }
      
      if (event.key.toLowerCase() === key.toLowerCase() && event.ctrlKey === ctrlKey) {
        event.preventDefault();
        callback();
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [key, callback, ctrlKey]);
}
