import { useEffect, useState } from 'react';
import type { FormEvent, ReactNode } from 'react';
import bgImage from '../../assets/homeScreenBack.jpg';
import Navbar from '../../components/layout/Navbar';
import Achiv from '../../components/ui/Achiv';
import ProgressBar from '../../components/ui/Reward';
import astra from '../../assets/astra2.png';
import astraAvatar from '../../assets/astra.png';

type ModalType = 'edit' | 'settings' | 'friends' | 'share' | null;
type VisibilitySetting = 'public' | 'friends' | 'private';
type FriendStatus = 'En linea' | 'Estudiando' | 'Descansando';

interface ProfileState {
  name: string;
  username: string;
  description: string;
  level: number;
  xp: number;
  joinDate: string;
  achievements: number;
  friends: number;
  streak: number;
  tasksCompleted: number;
  focusHours: string;
}

interface ProfileSettingsState {
  reminders: boolean;
  weeklySummary: boolean;
  friendRequests: boolean;
  showAchievements: boolean;
  shareStats: boolean;
  visibility: VisibilitySetting;
}

interface FriendCardData {
  id: number;
  name: string;
  username: string;
  level: number;
  status: FriendStatus;
  note: string;
}

interface SurfaceCardProps {
  eyebrow: string;
  title: string;
  children: ReactNode;
  className?: string;
}

interface ActionButtonProps {
  title: string;
  icon: ReactNode;
  onClick: () => void;
}

interface IconBadgeProps {
  children: ReactNode;
}

interface ProfileModalProps {
  title: string;
  subtitle: string;
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
  footer?: ReactNode;
}

interface ToggleRowProps {
  title: string;
  description: string;
  checked: boolean;
  onToggle: () => void;
}

const initialProfile: ProfileState = {
  name: 'Astra',
  username: 'astra',
  description:
    'Mascota oficial de Astrais. Ordeno tareas, acompano grupos y convierto cada racha completada en una pequena celebracion.',
  level: 3,
  xp: 45,
  joinDate: '16 diciembre 2025',
  achievements: 15,
  friends: 7,
  streak: 12,
  tasksCompleted: 38,
  focusHours: '23 h'
};

const initialSettings: ProfileSettingsState = {
  reminders: true,
  weeklySummary: true,
  friendRequests: true,
  showAchievements: true,
  shareStats: true,
  visibility: 'public'
};

const profileFriends: FriendCardData[] = [
  {
    id: 1,
    name: 'Noa',
    username: 'noa.dev',
    level: 5,
    status: 'En linea',
    note: 'Siempre se apunta a retos nuevos.'
  },
  {
    id: 2,
    name: 'Luna',
    username: 'luna.study',
    level: 4,
    status: 'Estudiando',
    note: 'Comparte grupos de productividad contigo.'
  },
  {
    id: 3,
    name: 'Kai',
    username: 'kai.quest',
    level: 6,
    status: 'Descansando',
    note: 'Te ayuda a mantener la racha semanal.'
  },
  {
    id: 4,
    name: 'Mia',
    username: 'mia.focus',
    level: 2,
    status: 'En linea',
    note: 'Acaba de desbloquear un logro raro.'
  }
];

const surfaceCardClassName =
  'rounded-2xl border border-white/15 bg-[linear-gradient(150deg,#8B5CF6bf,#1E4A6360)] p-5 shadow-[0_15px_32px_#090b1f59] backdrop-blur-sm';

const visibilityText: Record<VisibilitySetting, string> = {
  public: 'Perfil publico',
  friends: 'Solo amigos',
  private: 'Perfil privado'
};

const visibilityDescription: Record<VisibilitySetting, string> = {
  public: 'Tu enlace se puede compartir libremente.',
  friends: 'Solo tus amistades podran verlo completo.',
  private: 'Usa compartir solo como referencia privada.'
};

function SurfaceCard({ eyebrow, title, children, className = '' }: SurfaceCardProps) {
  return (
    <article className={`${surfaceCardClassName} ${className}`}>
      <header className="mb-4">
        <p className="pb-2 text-[0.78rem] uppercase tracking-[0.08em] text-[#c9b7ff]">{eyebrow}</p>
        <h2 className="font-['Press_Start_2P'] text-base leading-6 sm:text-lg">{title}</h2>
      </header>
      {children}
    </article>
  );
}

function IconBadge({ children }: IconBadgeProps) {
  return (
    <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-white/15 bg-white/10 text-white">
      {children}
    </span>
  );
}

function ActionButton({ title, icon, onClick }: ActionButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex min-h-26 flex-col justify-between items-center rounded-2xl border border-white/15 bg-white/10 p-4 text-left transition-colors duration-200 hover:bg-white/15"
    >
      <IconBadge>{icon}</IconBadge>
      <div className="space-y-1 flex flex-col w-full items-center text-center">
        <p className="font-semibold text-white">{title}</p>
      </div>
    </button>
  );
}

function ProfileModal({ title, subtitle, isOpen, onClose, children, footer }: ProfileModalProps) {
  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-[#090b1fcc] px-4 py-6 backdrop-blur-sm"
      onClick={onClose}
    >
      <section
        className="w-full max-w-3xl overflow-hidden rounded-3xl border border-white/15 bg-[linear-gradient(160deg,#0F3547f2,#8B5CF6d9)] shadow-[0_22px_60px_#090b1fb3]"
        onClick={(event) => event.stopPropagation()}
      >
        <header className="flex items-start justify-between gap-4 border-b border-white/10 px-6 py-5">
          <div>
            <p className="pb-2 text-[0.78rem] uppercase tracking-[0.08em] text-[#c9b7ff]">Perfil</p>
            <h2 className="font-['Press_Start_2P'] text-base leading-6 sm:text-lg">{title}</h2>
            <p className="mt-2 text-sm text-white/75">{subtitle}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full border border-white/15 bg-white/10 p-3 transition-colors duration-200 hover:bg-white/15"
            aria-label="Cerrar modal"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 6l12 12M18 6L6 18" />
            </svg>
          </button>
        </header>

        <div className="max-h-[70vh] overflow-y-auto px-6 py-5">{children}</div>

        {footer ? <div className="border-t border-white/10 px-6 py-4">{footer}</div> : null}
      </section>
    </div>
  );
}

function ToggleRow({ title, description, checked, onToggle }: ToggleRowProps) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-2xl border border-white/10 bg-white/10 px-4 py-4">
      <div className="space-y-1">
        <p className="font-semibold text-white">{title}</p>
        <p className="text-sm text-white/70">{description}</p>
      </div>
      <button
        type="button"
        onClick={onToggle}
        className={`relative h-8 w-15 rounded-full border transition-colors duration-200 ${
          checked ? 'border-accent-mint-300 bg-accent-mint-400/80' : 'border-white/20 bg-white/10'
        }`}
        aria-pressed={checked}
        aria-label={title}
      >
        <span
          className={`absolute top-1 right-9 h-5.5 w-5.5 rounded-full bg-white transition-transform duration-200 ${
            checked ? 'translate-x-8' : 'translate-x-1'
          }`}
        />
      </button>
    </div>
  );
}

const getFriendStatusClassName = (status: FriendStatus) => {
  if (status === 'En linea') return 'bg-state-success/20 text-state-success';
  if (status === 'Estudiando') return 'bg-state-warning/20 text-state-warning';
  return 'bg-state-info/20 text-state-info';
};

export default function Profile() {
  const [activeModal, setActiveModal] = useState<ModalType>(null);
  const [profile, setProfile] = useState<ProfileState>(initialProfile);
  const [settings, setSettings] = useState<ProfileSettingsState>(initialSettings);
  const [draftProfile, setDraftProfile] = useState<ProfileState>(initialProfile);
  const [draftSettings, setDraftSettings] = useState<ProfileSettingsState>(initialSettings);
  const [statusMessage, setStatusMessage] = useState<string>('');
  const [shareFeedback, setShareFeedback] = useState<string>('');

  useEffect(() => {
    if (!activeModal) return;

    const previousOverflow = document.body.style.overflow;
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setActiveModal(null);
      }
    };

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleEscape);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener('keydown', handleEscape);
    };
  }, [activeModal]);

  useEffect(() => {
    if (!statusMessage) return;

    const timeoutId = window.setTimeout(() => setStatusMessage(''), 3200);
    return () => window.clearTimeout(timeoutId);
  }, [statusMessage]);

  useEffect(() => {
    if (!shareFeedback) return;

    const timeoutId = window.setTimeout(() => setShareFeedback(''), 2600);
    return () => window.clearTimeout(timeoutId);
  }, [shareFeedback]);

  const openModal = (modal: Exclude<ModalType, null>) => {
    if (modal === 'edit') {
      setDraftProfile(profile);
    }

    if (modal === 'settings') {
      setDraftSettings(settings);
    }

    if (modal === 'share') {
      setShareFeedback('');
    }

    setActiveModal(modal);
  };

  const closeModal = () => {
    setActiveModal(null);
    setShareFeedback('');
  };

  const handleSaveProfile = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const cleanedName = draftProfile.name.trim();
    const cleanedUsername = draftProfile.username
      .trim()
      .toLowerCase()
      .replace(/^@+/, '')
      .replace(/\s+/g, '')
      .replace(/[^a-z0-9._-]/g, '');
    const cleanedDescription = draftProfile.description.trim();

    if (!cleanedName || !cleanedUsername) {
      setStatusMessage('Necesitas un nombre y un username validos para guardar el perfil.');
      return;
    }

    setProfile((currentProfile) => ({
      ...currentProfile,
      name: cleanedName,
      username: cleanedUsername,
      description: cleanedDescription || currentProfile.description
    }));
    setStatusMessage('Perfil actualizado correctamente.');
    closeModal();
  };

  const handleSaveSettings = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSettings(draftSettings);
    setStatusMessage('Ajustes guardados.');
    closeModal();
  };

  const profileUrl =
    typeof window === 'undefined'
      ? `/profile?user=${profile.username}`
      : `${window.location.origin}/profile?user=${profile.username}`;

  const shareText = settings.shareStats
    ? `${profile.name} (@${profile.username}) tiene nivel ${profile.level}, ${profile.achievements} logros y una racha de ${profile.streak} dias en Astrais.`
    : `${profile.name} (@${profile.username}) comparte su perfil de Astrais contigo.`;

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(profileUrl);
      setShareFeedback('Enlace copiado al portapapeles.');
    } catch {
      setShareFeedback('No pude copiar el enlace automaticamente.');
    }
  };

  const handleCopySummary = async () => {
    try {
      await navigator.clipboard.writeText(`${shareText} ${profileUrl}`);
      setShareFeedback('Resumen del perfil copiado.');
    } catch {
      setShareFeedback('No pude copiar el resumen automaticamente.');
    }
  };

  const handleNativeShare = async () => {
    if (!navigator.share) {
      await handleCopyLink();
      return;
    }

    try {
      await navigator.share({
        title: `${profile.name} en Astrais`,
        text: shareText,
        url: profileUrl
      });
      setShareFeedback('Perfil compartido.');
    } catch (error) {
      if (error instanceof Error && error.name === 'AbortError') {
        return;
      }

      setShareFeedback('No pude abrir el menu de compartir.');
    }
  };

  return (
    <main
      style={{ backgroundImage: `url(${bgImage})` }}
      className="relative min-h-screen bg-cover bg-center font-['Space_Grotesk'] text-white"
    >
      <Navbar />

      <section className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-4 py-6">
        <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.65fr_1fr]">
          <article className={`${surfaceCardClassName} relative overflow-hidden px-6 py-6`}>
            <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,#9FE8C533,transparent_36%),radial-gradient(circle_at_bottom_left,#D9D9D926,transparent_28%)]" />
            <img
              src={astraAvatar}
              alt="Mascota Astrais"
              className="pointer-events-none absolute -right-10 -top-2 hidden h-42 opacity-90 lg:block"
            />

            <div className="relative flex flex-col gap-6">
              <header className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div>
                  <p className="pb-2 text-[0.78rem] uppercase tracking-[0.08em] text-[#c9b7ff]">Perfil de usuario</p>
                  <h1 className="font-['Press_Start_2P'] text-xl leading-7 sm:text-2xl">{profile.name}</h1>
                  <div className="mt-3 flex flex-wrap items-center gap-2 text-sm text-white/75">
                    <span className="rounded-full border border-white/15 bg-black/20 px-3 py-1">@{profile.username}</span>
                    <span className="rounded-full border border-state-warning/30 bg-state-warning/15 px-3 py-1 text-state-warning">
                      Nivel {profile.level}
                    </span>
                    <span className="rounded-full border border-accent-mint-300/35 bg-accent-mint-300/10 px-3 py-1 text-accent-mint-300">
                      {visibilityText[settings.visibility]}
                    </span>
                  </div>
                </div>

                {statusMessage ? (
                  <span className="rounded-full border border-accent-mint-300/30 bg-accent-mint-300/15 px-4 py-2 text-sm text-accent-mint-300">
                    {statusMessage}
                  </span>
                ) : null}
              </header>

              <div className="grid grid-cols-1 gap-6 lg:grid-cols-[auto_1fr]">
                <div className="flex flex-col items-center gap-3">
                  <div className="rounded-full border border-white/15 bg-accent-beige-300/20 p-3 shadow-[0_10px_24px_#090b1f45]">
                    <div className="rounded-full bg-white/95 p-2">
                      <img src={astra} alt="Avatar de Astra" className="w-28 sm:w-34" />
                    </div>
                  </div>
                  <div className="rounded-full border border-white/15 bg-black/25 px-3 py-1 text-sm text-white/80">
                    Miembro desde {profile.joinDate}
                  </div>
                </div>

                <div className="flex flex-col gap-5">
                  <div className="space-y-3">
                    <p className="text-base leading-7 text-white/85">{profile.description}</p>
                    <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                      <div className="mb-2 flex items-center justify-between gap-3 text-sm">
                        <span className="font-semibold text-white">Progreso al siguiente nivel</span>
                        <span className="text-white/70">{profile.xp}/100 XP</span>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="text-state-warning">
                          <svg className="h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 2l2.9 6.26 6.85.62-5.2 4.55 1.53 6.77L12 16.89 5.92 20.2l1.53-6.77-5.2-4.55 6.85-.62L12 2z" />
                          </svg>
                        </span>
                        <ProgressBar value={profile.xp} />
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
                    <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-3">
                      <p className="text-xs uppercase tracking-[0.08em] text-white/60">Logros</p>
                      <p className="mt-2 text-2xl font-semibold">{profile.achievements}</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-3">
                      <p className="text-xs uppercase tracking-[0.08em] text-white/60">Amigos</p>
                      <p className="mt-2 text-2xl font-semibold">{profile.friends}</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-3">
                      <p className="text-xs uppercase tracking-[0.08em] text-white/60">Racha</p>
                      <p className="mt-2 text-2xl font-semibold">{profile.streak} dias</p>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-3">
                      <p className="text-xs uppercase tracking-[0.08em] text-white/60">Focus</p>
                      <p className="mt-2 text-2xl font-semibold">{profile.focusHours}</p>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
                    <ActionButton
                      title="Editar perfil"
                      onClick={() => openModal('edit')}
                      icon={
                        <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M16.86 3.49a2.1 2.1 0 012.97 2.97L8 18.29 3 20l1.71-5L16.86 3.49z" />
                        </svg>
                      }
                    />
                    <ActionButton
                      title="Ajustes"
                      onClick={() => openModal('settings')}
                      icon={
                        <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M10.33 2.2l.31 1.86a7.96 7.96 0 012.72 0l.31-1.86 2.53.74-.62 1.8c.82.43 1.57 1 2.2 1.64l1.7-.74 1.27 2.3-1.49 1.1c.17.47.3.96.38 1.47l1.9.3v2.6l-1.9.3a8.24 8.24 0 01-.38 1.47l1.49 1.1-1.27 2.3-1.7-.74a8.05 8.05 0 01-2.2 1.64l.62 1.8-2.53.74-.31-1.86a7.96 7.96 0 01-2.72 0l-.31 1.86-2.53-.74.62-1.8a8.05 8.05 0 01-2.2-1.64l-1.7.74-1.27-2.3 1.49-1.1a8.24 8.24 0 01-.38-1.47L2.2 13.3v-2.6l1.9-.3c.08-.51.21-1 .38-1.47L2.99 7.83l1.27-2.3 1.7.74a8.05 8.05 0 012.2-1.64l-.62-1.8 2.53-.74z" />
                          <path strokeLinecap="round" strokeLinejoin="round" d="M12 15.25A3.25 3.25 0 1012 8.75a3.25 3.25 0 000 6.5z" />
                        </svg>
                      }
                    />
                    <ActionButton
                      title="Compartir"
                      onClick={() => openModal('share')}
                      icon={
                        <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M8 12a3 3 0 013-3h5" />
                          <path strokeLinecap="round" strokeLinejoin="round" d="M14 6l5 5-5 5" />
                          <path strokeLinecap="round" strokeLinejoin="round" d="M17 12H9a4 4 0 00-4 4v2" />
                        </svg>
                      }
                    />
                    <ActionButton
                      title="Ver amigos"
                      onClick={() => openModal('friends')}
                      icon={
                        <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M16 11a4 4 0 10-4-4 4 4 0 004 4zM8 13a3 3 0 10-3-3 3 3 0 003 3z" />
                          <path strokeLinecap="round" strokeLinejoin="round" d="M16 13c3.31 0 6 1.57 6 3.5V18H10v-1.5c0-1.93 2.69-3.5 6-3.5zM8 14c1.5 0 2.85.32 3.85.85" />
                        </svg>
                      }
                    />
                  </div>
                </div>
              </div>
            </div>
          </article>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-1">
            <SurfaceCard eyebrow="Resumen" title="Estadisticas destacadas">
              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.08em] text-white/60">Tareas completadas</p>
                  <p className="mt-3 text-3xl font-semibold">{profile.tasksCompleted}</p>
                </div>
                <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.08em] text-white/60">Horas de foco</p>
                  <p className="mt-3 text-3xl font-semibold">{profile.focusHours}</p>
                </div>
                <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.08em] text-white/60">Logros raros</p>
                  <p className="mt-3 text-3xl font-semibold">4</p>
                </div>
                <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.08em] text-white/60">Grupos activos</p>
                  <p className="mt-3 text-3xl font-semibold">3</p>
                </div>
              </div>
            </SurfaceCard>

            <SurfaceCard eyebrow="Ajustes activos" title="Privacidad y avisos">
              <div className="flex flex-col gap-3">
                <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-3">
                  <p className="font-semibold text-white">{visibilityText[settings.visibility]}</p>
                  <p className="mt-1 text-sm text-white/70">{visibilityDescription[settings.visibility]}</p>
                </div>

                <button
                  type="button"
                  onClick={() => openModal('settings')}
                  className="mt-2 rounded-2xl border border-accent-beige-300/20 bg-accent-beige-300/20 px-4 py-3 text-left text-white transition-colors duration-200 hover:bg-accent-beige-300/30"
                >
                  Ajustar preferencias del perfil
                </button>
              </div>
            </SurfaceCard>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1.1fr_0.9fr]">
          <SurfaceCard eyebrow="Coleccion" title="Logros destacados">
            {settings.showAchievements ? (
              <div className="space-y-5">
                <div className="flex flex-wrap gap-3">
                  <Achiv />
                  <Achiv />
                  <Achiv />
                  <Achiv />
                </div>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                    <p className="font-semibold text-white">Constelacion ordenada</p>
                    <p className="mt-2 text-sm text-white/70">Completa una semana seguida de tareas diarias.</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
                    <p className="font-semibold text-white">Maestra de grupos</p>
                    <p className="mt-2 text-sm text-white/70">Participa activamente en tres grupos distintos.</p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-white/15 bg-black/20 px-4 py-8 text-center">
                <p className="font-semibold text-white">Tus logros estan ocultos en este momento.</p>
                <p className="mt-2 text-sm text-white/70">
                  Vuelve a activarlos desde ajustes si quieres mostrarlos en tu perfil publico.
                </p>
                <button
                  type="button"
                  onClick={() => openModal('settings')}
                  className="mt-4 rounded-2xl border border-white/15 bg-white/10 px-4 py-3 text-white transition-colors duration-200 hover:bg-white/15"
                >
                  Abrir ajustes
                </button>
              </div>
            )}
          </SurfaceCard>

          <SurfaceCard eyebrow="Comunidad" title="Tu circulo activo">
            <div className="space-y-3">
              {profileFriends.slice(0, 3).map((friend) => (
                <div
                  key={friend.id}
                  className="flex items-center justify-between gap-4 rounded-2xl border border-white/10 bg-black/20 px-4 py-4"
                >
                  <div>
                    <p className="font-semibold text-white">{friend.name}</p>
                    <p className="text-sm text-white/65">@{friend.username}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className={`rounded-full px-3 py-1 text-xs ${getFriendStatusClassName(friend.status)}`}>
                      {friend.status}
                    </span>
                    <span className="rounded-full border border-white/15 bg-white/10 px-3 py-1 text-sm text-white/80">
                      Nv. {friend.level}
                    </span>
                  </div>
                </div>
              ))}
              <button
                type="button"
                onClick={() => openModal('friends')}
                className="w-full rounded-2xl border border-white/15 bg-white/10 px-4 py-3 text-left transition-colors duration-200 hover:bg-white/15"
              >
                Ver lista completa de amistades
              </button>
            </div>
          </SurfaceCard>
        </div>
      </section>

      <ProfileModal
        title="Editar perfil"
        subtitle="Actualiza tu identidad visible y la descripcion que acompana tu perfil."
        isOpen={activeModal === 'edit'}
        onClose={closeModal}
        footer={
          <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={closeModal}
              className="rounded-2xl border border-white/15 bg-white/10 px-4 py-3 text-white transition-colors duration-200 hover:bg-white/15"
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="profile-edit-form"
              className="rounded-2xl border border-accent-beige-300/20 bg-accent-beige-300/90 px-4 py-3 font-semibold text-primary-900 transition-colors duration-200 hover:bg-accent-beige-300"
            >
              Guardar cambios
            </button>
          </div>
        }
      >
        <form id="profile-edit-form" className="grid grid-cols-1 gap-4 lg:grid-cols-2" onSubmit={handleSaveProfile}>
          <label className="flex flex-col gap-2">
            <span className="text-sm font-semibold text-white">Nombre visible</span>
            <input
              type="text"
              value={draftProfile.name}
              onChange={(event) => setDraftProfile((currentProfile) => ({ ...currentProfile, name: event.target.value }))}
              className="rounded-2xl border border-white/15 bg-black/20 px-4 py-3 text-white outline-none transition-colors duration-200 focus:border-accent-mint-300"
              placeholder="Astra"
            />
          </label>

          <label className="flex flex-col gap-2">
            <span className="text-sm font-semibold text-white">Username</span>
            <div className="flex items-center rounded-2xl border border-white/15 bg-black/20 px-4 py-3 focus-within:border-accent-mint-300">
              <span className="mr-2 text-white/60">@</span>
              <input
                type="text"
                value={draftProfile.username}
                onChange={(event) => setDraftProfile((currentProfile) => ({ ...currentProfile, username: event.target.value }))}
                className="w-full bg-transparent text-white outline-none"
                placeholder="astra"
              />
            </div>
          </label>

          <label className="flex flex-col gap-2 lg:col-span-2">
            <span className="text-sm font-semibold text-white">Descripcion</span>
            <textarea
              rows={5}
              value={draftProfile.description}
              onChange={(event) => setDraftProfile((currentProfile) => ({ ...currentProfile, description: event.target.value }))}
              className="resize-none rounded-2xl border border-white/15 bg-black/20 px-4 py-3 text-white outline-none transition-colors duration-200 focus:border-accent-mint-300"
              placeholder="Cuenta un poco quien eres en Astrais."
            />
          </label>

          <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4 lg:col-span-2">
            <p className="text-sm font-semibold text-white">Vista previa rapida</p>
            <p className="mt-3 text-lg font-semibold text-white">{draftProfile.name || 'Tu nombre'}</p>
            <p className="text-sm text-white/65">@{draftProfile.username || 'username'}</p>
            <p className="mt-3 text-sm leading-6 text-white/80">
              {draftProfile.description || 'Tu descripcion aparecera aqui cuando empieces a escribir.'}
            </p>
          </div>
        </form>
      </ProfileModal>

      <ProfileModal
        title="Ajustes del perfil"
        subtitle="Controla visibilidad, notificaciones y lo que decides compartir desde tu cuenta."
        isOpen={activeModal === 'settings'}
        onClose={closeModal}
        footer={
          <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={() => setDraftSettings(initialSettings)}
              className="rounded-2xl border border-white/15 bg-white/10 px-4 py-3 text-white transition-colors duration-200 hover:bg-white/15"
            >
              Restablecer
            </button>
            <button
              type="submit"
              form="profile-settings-form"
              className="rounded-2xl border border-accent-beige-300/20 bg-accent-beige-300/90 px-4 py-3 font-semibold text-primary-900 transition-colors duration-200 hover:bg-accent-beige-300"
            >
              Guardar ajustes
            </button>
          </div>
        }
      >
        <form id="profile-settings-form" className="space-y-5" onSubmit={handleSaveSettings}>
          <div className="space-y-3">
            <p className="text-sm font-semibold uppercase tracking-[0.08em] text-[#c9b7ff]">Privacidad</p>
            <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
              {(['public', 'friends', 'private'] as VisibilitySetting[]).map((option) => (
                <button
                  key={option}
                  type="button"
                  onClick={() => setDraftSettings((currentSettings) => ({ ...currentSettings, visibility: option }))}
                  className={`rounded-2xl border px-4 py-4 text-left transition-colors duration-200 ${
                    draftSettings.visibility === option
                      ? 'border-accent-mint-300 bg-accent-mint-300/15'
                      : 'border-white/10 bg-black/20 hover:bg-white/10'
                  }`}
                >
                  <p className="font-semibold text-white">{visibilityText[option]}</p>
                  <p className="mt-2 text-sm text-white/70">{visibilityDescription[option]}</p>
                </button>
              ))}
            </div>
          </div>

          <div className="space-y-3">
            <p className="text-sm font-semibold uppercase tracking-[0.08em] text-[#c9b7ff]">Actividad</p>
            <ToggleRow
              title="Recordatorios del perfil"
              description="Muestra avisos cuando tengas metas o tareas importantes pendientes."
              checked={draftSettings.reminders}
              onToggle={() =>
                setDraftSettings((currentSettings) => ({ ...currentSettings, reminders: !currentSettings.reminders }))
              }
            />
            <ToggleRow
              title="Resumen semanal"
              description="Recibe un resumen con tus progresos, logros y rachas destacadas."
              checked={draftSettings.weeklySummary}
              onToggle={() =>
                setDraftSettings((currentSettings) => ({
                  ...currentSettings,
                  weeklySummary: !currentSettings.weeklySummary
                }))
              }
            />
            <ToggleRow
              title="Solicitudes de amistad"
              description="Permite que otras personas puedan encontrarte y enviarte invitaciones."
              checked={draftSettings.friendRequests}
              onToggle={() =>
                setDraftSettings((currentSettings) => ({
                  ...currentSettings,
                  friendRequests: !currentSettings.friendRequests
                }))
              }
            />
            <ToggleRow
              title="Mostrar logros en el perfil"
              description="Ensenia tus insignias destacadas dentro de la pagina principal del perfil."
              checked={draftSettings.showAchievements}
              onToggle={() =>
                setDraftSettings((currentSettings) => ({
                  ...currentSettings,
                  showAchievements: !currentSettings.showAchievements
                }))
              }
            />
            <ToggleRow
              title="Compartir estadisticas"
              description="Incluye nivel, racha y logros cuando uses la accion de compartir."
              checked={draftSettings.shareStats}
              onToggle={() =>
                setDraftSettings((currentSettings) => ({
                  ...currentSettings,
                  shareStats: !currentSettings.shareStats
                }))
              }
            />
          </div>
        </form>
      </ProfileModal>

      <ProfileModal
        title="Compartir perfil"
        subtitle="Comparte tu progreso con enlace directo, resumen corto o el menu nativo del navegador."
        isOpen={activeModal === 'share'}
        onClose={closeModal}
      >
        <div className="space-y-4">
          <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
            <p className="text-sm font-semibold text-white">Enlace del perfil</p>
            <p className="mt-3 break-all rounded-2xl border border-white/10 bg-black/20 px-4 py-3 text-sm text-white/75">
              {profileUrl}
            </p>
            <p className="mt-3 text-sm text-white/70">{visibilityDescription[settings.visibility]}</p>
          </div>

          <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
            <button
              type="button"
              onClick={handleCopyLink}
              className="flex flex-row items-center justify-center rounded-2xl border border-white/15 bg-white/10 px-4 py-4 text-left transition-colors duration-200 hover:bg-white/15"
            >
              <p className="font-semibold text-white">Copiar enlace</p>
            </button>
            <button
              type="button"
              onClick={handleCopySummary}
              className="flex flex-row items-center justify-center rounded-2xl border border-white/15 bg-white/10 px-4 py-4 text-left transition-colors duration-200 hover:bg-white/15"
            >
              <p className="font-semibold text-white">Copiar resumen</p>
            </button>
            <button
              type="button"
              onClick={handleNativeShare}
              className="flex flex-row items-center justify-center rounded-2xl border border-accent-beige-300/20 bg-accent-beige-300/20 px-4 py-4  text-primary-900 transition-colors duration-200 hover:bg-accent-beige-300/30"
            >
              <p className="font-semibold text-white">Compartir</p>
            </button>
          </div>

          <div className="rounded-2xl border border-white/10 bg-black/20 px-4 py-4">
            <p className="text-sm font-semibold text-white">Vista previa del texto compartido</p>
            <p className="mt-3 text-sm leading-6 text-white/80">{shareText}</p>
          </div>

          {shareFeedback ? (
            <div className="rounded-2xl border border-accent-mint-300/30 bg-accent-mint-300/15 px-4 py-3 text-sm text-accent-mint-300">
              {shareFeedback}
            </div>
          ) : null}
        </div>
      </ProfileModal>

      <ProfileModal
        title="Tus amistades"
        subtitle="Una vista rapida de quienes estan conectados contigo y como va su actividad."
        isOpen={activeModal === 'friends'}
        onClose={closeModal}
      >
        <div className="space-y-3">
          {profileFriends.map((friend) => (
            <div
              key={friend.id}
              className="flex flex-col gap-3 rounded-2xl border border-white/10 bg-black/20 px-4 py-4 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <p className="font-semibold text-white">{friend.name}</p>
                  <span className="rounded-full border border-white/15 bg-white/10 px-3 py-1 text-sm text-white/80">
                    Nv. {friend.level}
                  </span>
                </div>
                <p className="mt-1 text-sm text-white/65">@{friend.username}</p>
                <p className="mt-2 text-sm text-white/75">{friend.note}</p>
              </div>
              <span className={`w-fit rounded-full px-3 py-1 text-sm ${getFriendStatusClassName(friend.status)}`}>
                {friend.status}
              </span>
            </div>
          ))}
        </div>
      </ProfileModal>
    </main>
  );
}
