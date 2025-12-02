import { Translations } from '../types';

/**
 * Format date for display
 */
export function formatTime(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

/**
 * Format date with day information
 */
export function formatDate(date: Date | string, translations: Translations): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  
  // Check if today
  if (isSameDay(d, now)) {
    return translations.today;
  }
  
  // Check if yesterday
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (isSameDay(d, yesterday)) {
    return translations.yesterday;
  }
  
  // Return formatted date
  return d.toLocaleDateString([], { 
    month: 'short', 
    day: 'numeric',
    year: d.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
  });
}

/**
 * Check if two dates are the same day
 */
function isSameDay(date1: Date, date2: Date): boolean {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
}

/**
 * Format relative time (e.g., "2 minutes ago")
 */
export function formatRelativeTime(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diffMs = now.getTime() - d.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);
  const diffMinutes = Math.floor(diffSeconds / 60);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSeconds < 60) {
    return 'just now';
  } else if (diffMinutes < 60) {
    return `${diffMinutes} minute${diffMinutes > 1 ? 's' : ''} ago`;
  } else if (diffHours < 24) {
    return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
  } else if (diffDays < 7) {
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  } else {
    return formatDate(d, getDefaultTranslations());
  }
}

/**
 * Format file size for display
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * Truncate text with ellipsis
 */
export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength - 3) + '...';
}

/**
 * Generate a unique ID
 */
export function generateId(): string {
  return 'sd-' + Math.random().toString(36).substring(2, 11);
}

/**
 * Debounce function execution
 */
export function debounce<T extends (...args: unknown[]) => void>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null;
  
  return function(this: unknown, ...args: Parameters<T>) {
    if (timeout) {
      clearTimeout(timeout);
    }
    timeout = setTimeout(() => {
      func.apply(this, args);
    }, wait);
  };
}

/**
 * Get default translations (English)
 */
export function getDefaultTranslations(): Translations {
  return {
    greeting: 'Hi! How can we help you?',
    placeholder: 'Type your message...',
    offlineMessage: 'We are currently offline. Leave a message and we\'ll get back to you soon!',
    sendButton: 'Send',
    attachFile: 'Attach file',
    startChat: 'Start chat',
    endChat: 'End chat',
    minimize: 'Minimize',
    close: 'Close',
    nameLabel: 'Your name',
    emailLabel: 'Your email',
    messageLabel: 'Your message',
    namePlaceholder: 'Enter your name',
    emailPlaceholder: 'Enter your email',
    messagePlaceholder: 'How can we help you?',
    submitForm: 'Start conversation',
    connecting: 'Connecting...',
    connected: 'Connected',
    disconnected: 'Disconnected',
    reconnecting: 'Reconnecting...',
    agentTyping: 'Agent is typing...',
    agentJoined: 'Agent joined the conversation',
    sessionClosed: 'Conversation ended',
    fileUploadError: 'Failed to upload file',
    fileTooLarge: 'File is too large',
    invalidFileType: 'Invalid file type',
    sending: 'Sending...',
    sent: 'Sent',
    delivered: 'Delivered',
    read: 'Read',
    today: 'Today',
    yesterday: 'Yesterday'
  };
}

/**
 * Get Russian translations
 */
export function getRussianTranslations(): Translations {
  return {
    greeting: 'Привет! Чем можем помочь?',
    placeholder: 'Введите сообщение...',
    offlineMessage: 'Мы сейчас офлайн. Оставьте сообщение, и мы свяжемся с вами в ближайшее время!',
    sendButton: 'Отправить',
    attachFile: 'Прикрепить файл',
    startChat: 'Начать чат',
    endChat: 'Завершить чат',
    minimize: 'Свернуть',
    close: 'Закрыть',
    nameLabel: 'Ваше имя',
    emailLabel: 'Ваш email',
    messageLabel: 'Ваше сообщение',
    namePlaceholder: 'Введите ваше имя',
    emailPlaceholder: 'Введите ваш email',
    messagePlaceholder: 'Чем можем помочь?',
    submitForm: 'Начать разговор',
    connecting: 'Подключение...',
    connected: 'Подключено',
    disconnected: 'Отключено',
    reconnecting: 'Переподключение...',
    agentTyping: 'Оператор печатает...',
    agentJoined: 'Оператор присоединился к разговору',
    sessionClosed: 'Разговор завершен',
    fileUploadError: 'Ошибка загрузки файла',
    fileTooLarge: 'Файл слишком большой',
    invalidFileType: 'Неверный тип файла',
    sending: 'Отправка...',
    sent: 'Отправлено',
    delivered: 'Доставлено',
    read: 'Прочитано',
    today: 'Сегодня',
    yesterday: 'Вчера'
  };
}

/**
 * Get Uzbek translations
 */
export function getUzbekTranslations(): Translations {
  return {
    greeting: 'Salom! Sizga qanday yordam bera olamiz?',
    placeholder: 'Xabar yozing...',
    offlineMessage: 'Biz hozir oflaynmiz. Xabar qoldiring, tez orada siz bilan bog\'lanamiz!',
    sendButton: 'Yuborish',
    attachFile: 'Fayl biriktirish',
    startChat: 'Suhbatni boshlash',
    endChat: 'Suhbatni tugatish',
    minimize: 'Kichraytirish',
    close: 'Yopish',
    nameLabel: 'Ismingiz',
    emailLabel: 'Emailingiz',
    messageLabel: 'Xabaringiz',
    namePlaceholder: 'Ismingizni kiriting',
    emailPlaceholder: 'Emailingizni kiriting',
    messagePlaceholder: 'Sizga qanday yordam bera olamiz?',
    submitForm: 'Suhbatni boshlash',
    connecting: 'Ulanmoqda...',
    connected: 'Ulangan',
    disconnected: 'Uzilgan',
    reconnecting: 'Qayta ulanmoqda...',
    agentTyping: 'Operator yozmoqda...',
    agentJoined: 'Operator suhbatga qo\'shildi',
    sessionClosed: 'Suhbat tugadi',
    fileUploadError: 'Faylni yuklashda xatolik',
    fileTooLarge: 'Fayl juda katta',
    invalidFileType: 'Noto\'g\'ri fayl turi',
    sending: 'Yuborilmoqda...',
    sent: 'Yuborildi',
    delivered: 'Yetkazildi',
    read: 'O\'qildi',
    today: 'Bugun',
    yesterday: 'Kecha'
  };
}

/**
 * Get translations by language code
 */
export function getTranslations(language: string): Translations {
  switch (language) {
    case 'ru':
      return getRussianTranslations();
    case 'uz':
      return getUzbekTranslations();
    case 'en':
    default:
      return getDefaultTranslations();
  }
}

/**
 * Validate email format
 */
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}
