export type ProfileIntent = 'citizen' | 'representative';

export function saveProfileIntent(userId: string, intent: ProfileIntent) {
  try {
    localStorage.setItem(`reboot.profileIntent.${userId}`, intent);
  } catch {
    // Registration also carries the preference in navigation state.
  }
}

export function getProfileIntent(userId: string): ProfileIntent {
  try {
    if (localStorage.getItem(`reboot.profileIntent.${userId}`) === 'representative') return 'representative';
  } catch {
    // Default to the directory when storage is unavailable.
  }
  return 'citizen';
}
