import { getSuggestionStatusClass, getSuggestionStatusLabel } from './moderationSuggestion.js';

export default function SuggestionStatusBadge({ status }) {
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${getSuggestionStatusClass(status)}`}>{getSuggestionStatusLabel(status)}</span>;
}
