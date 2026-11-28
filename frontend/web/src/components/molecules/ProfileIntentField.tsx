import { useId } from 'react';

export function ProfileIntentField() {
  const id = useId();
  return (
    <fieldset className="profile-intent">
      <legend>Choose your profile</legend>
      <label>
        <input type="radio" name="profileIntent" value="citizen" defaultChecked aria-describedby={`${id}-citizen`} />
        <span><strong>Individual Citizen</strong><small id={`${id}-citizen`}>Donate, repair, or recycle personal devices</small></span>
      </label>
      <label>
        <input type="radio" name="profileIntent" value="representative" aria-describedby={`${id}-representative`} />
        <span><strong>Organization Representative</strong><small id={`${id}-representative`}>Register and manage a recycling center, repair shop, or donating company</small></span>
      </label>
    </fieldset>
  );
}
