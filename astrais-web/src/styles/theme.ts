export interface ThemeConfig {
  primary: string
  secondary: string
  tertiary: string
  background: string
  backgroundAlt: string
  surface: string
  text: string
  error: string
}

export const DEFAULT_THEME_CONFIG: ThemeConfig = {
  primary: '#8B5CF6',
  secondary: '#38BDF8',
  tertiary: '#10B981',
  background: '#0D1117',
  backgroundAlt: '#11161D',
  surface: '#1A1D2D',
  text: '#F8FAFC',
  error: '#F43F5E',
}

const HEX_COLOR_PATTERN = /^#[0-9a-fA-F]{6}$/

function normalizeColor(value: unknown, fallback: string) {
  if (typeof value !== 'string') {
    return fallback
  }

  const trimmedValue = value.trim()
  return HEX_COLOR_PATTERN.test(trimmedValue) ? trimmedValue : fallback
}

export function parseThemeConfig(themeColors?: string | null): ThemeConfig {
  if (!themeColors) {
    return DEFAULT_THEME_CONFIG
  }

  try {
    const parsedTheme = JSON.parse(themeColors) as Partial<ThemeConfig>

    return {
      primary: normalizeColor(parsedTheme.primary, DEFAULT_THEME_CONFIG.primary),
      secondary: normalizeColor(parsedTheme.secondary, DEFAULT_THEME_CONFIG.secondary),
      tertiary: normalizeColor(parsedTheme.tertiary, DEFAULT_THEME_CONFIG.tertiary),
      background: normalizeColor(parsedTheme.background, DEFAULT_THEME_CONFIG.background),
      backgroundAlt: normalizeColor(parsedTheme.backgroundAlt, DEFAULT_THEME_CONFIG.backgroundAlt),
      surface: normalizeColor(parsedTheme.surface, DEFAULT_THEME_CONFIG.surface),
      text: normalizeColor(parsedTheme.text, DEFAULT_THEME_CONFIG.text),
      error: normalizeColor(parsedTheme.error, DEFAULT_THEME_CONFIG.error),
    }
  } catch {
    return DEFAULT_THEME_CONFIG
  }
}

export function applyThemeColors(themeColors?: string | null) {
  if (typeof document === 'undefined') {
    return
  }

  const theme = parseThemeConfig(themeColors)
  const root = document.documentElement

  root.style.setProperty('--astrais-primary', theme.primary)
  root.style.setProperty('--astrais-secondary', theme.secondary)
  root.style.setProperty('--astrais-tertiary', theme.tertiary)
  root.style.setProperty('--astrais-background', theme.background)
  root.style.setProperty('--astrais-background-alt', theme.backgroundAlt)
  root.style.setProperty('--astrais-surface', theme.surface)
  root.style.setProperty('--astrais-text', theme.text)
  root.style.setProperty('--astrais-error', theme.error)
  root.style.colorScheme = 'dark'
}
