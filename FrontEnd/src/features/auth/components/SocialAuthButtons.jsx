import Button from '../../../components/common/Button.jsx';

export default function SocialAuthButtons({ onUnavailable, actionLabel = 'Tiep tuc voi' }) {
  function clickSocial(providerName) {
    // OAuth khong thuoc MVP; nut chi giu tren UI de the hien huong phat trien sau nay.
    onUnavailable(providerName);
  }

  return (
    <div className="space-y-3">
      <Button type="button" variant="secondary" className="min-h-[44px] w-full gap-3" onClick={() => clickSocial('Google')}>
        <span className="font-black text-blue-600">G</span>
        {actionLabel} Google
      </Button>
      <Button type="button" variant="secondary" className="min-h-[44px] w-full gap-3" onClick={() => clickSocial('Facebook')}>
        <span className="font-black text-blue-700">f</span>
        {actionLabel} Facebook
      </Button>
    </div>
  );
}
