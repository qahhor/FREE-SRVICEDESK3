import { createElement } from '../utils/dom';
import { Translations } from '../types';

export interface FileUploadOptions {
  translations: Translations;
  maxFileSize: number;
  allowedTypes: string[];
  onFileSelect: (file: File) => void;
  onError: (error: string) => void;
}

/**
 * File upload component with drag & drop support
 */
export class FileUpload {
  private fileInput: HTMLInputElement;
  private dropZone: HTMLElement | null = null;
  private options: FileUploadOptions;
  private isDragging = false;

  constructor(options: FileUploadOptions) {
    this.options = options;
    this.fileInput = this.createFileInput();
  }

  /**
   * Get the file input element
   */
  getFileInput(): HTMLInputElement {
    return this.fileInput;
  }

  /**
   * Trigger file selection dialog
   */
  openFileDialog(): void {
    this.fileInput.click();
  }

  /**
   * Set up drop zone on an element
   */
  setupDropZone(container: HTMLElement): void {
    this.dropZone = createElement('div', { className: 'sd-widget-dropzone' });
    const text = createElement('span', { className: 'sd-widget-dropzone-text' }, ['Drop file here']);
    this.dropZone.appendChild(text);
    container.appendChild(this.dropZone);

    // Drag events
    container.addEventListener('dragenter', (e) => this.handleDragEnter(e));
    container.addEventListener('dragover', (e) => this.handleDragOver(e));
    container.addEventListener('dragleave', (e) => this.handleDragLeave(e));
    container.addEventListener('drop', (e) => this.handleDrop(e));
  }

  /**
   * Create hidden file input
   */
  private createFileInput(): HTMLInputElement {
    const input = createElement('input', {
      className: 'sd-widget-file-input',
      type: 'file',
      accept: this.options.allowedTypes.join(',')
    }) as HTMLInputElement;

    input.addEventListener('change', () => {
      const file = input.files?.[0];
      if (file) {
        this.validateAndUpload(file);
        input.value = '';
      }
    });

    return input;
  }

  /**
   * Validate and upload file
   */
  private validateAndUpload(file: File): void {
    // Validate file size
    if (file.size > this.options.maxFileSize) {
      this.options.onError(this.options.translations.fileTooLarge);
      return;
    }

    // Validate file type
    const extension = file.name.split('.').pop()?.toLowerCase();
    const mimeType = file.type;
    
    const isAllowed = this.options.allowedTypes.some(type => {
      if (type.startsWith('.')) {
        return extension === type.substring(1);
      }
      if (type.endsWith('/*')) {
        return mimeType.startsWith(type.slice(0, -1));
      }
      return mimeType === type;
    });

    if (!isAllowed) {
      this.options.onError(this.options.translations.invalidFileType);
      return;
    }

    this.options.onFileSelect(file);
  }

  /**
   * Handle drag enter
   */
  private handleDragEnter(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging = true;
    this.showDropZone();
  }

  /**
   * Handle drag over
   */
  private handleDragOver(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
  }

  /**
   * Handle drag leave
   */
  private handleDragLeave(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    
    // Check if we're leaving the container entirely
    const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
    const x = e.clientX;
    const y = e.clientY;
    
    if (x < rect.left || x >= rect.right || y < rect.top || y >= rect.bottom) {
      this.isDragging = false;
      this.hideDropZone();
    }
  }

  /**
   * Handle drop
   */
  private handleDrop(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging = false;
    this.hideDropZone();

    const files = e.dataTransfer?.files;
    if (files && files.length > 0) {
      this.validateAndUpload(files[0]);
    }
  }

  /**
   * Show drop zone overlay
   */
  private showDropZone(): void {
    if (this.dropZone) {
      this.dropZone.classList.add('is-active');
    }
  }

  /**
   * Hide drop zone overlay
   */
  private hideDropZone(): void {
    if (this.dropZone) {
      this.dropZone.classList.remove('is-active');
    }
  }
}
