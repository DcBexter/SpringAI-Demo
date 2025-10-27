import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ChatComponent } from './chat';

describe('ChatComponent', () => {
  let component: ChatComponent;
  let fixture: ComponentFixture<ChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatComponent, BrowserAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate a conversation ID on init', () => {
    expect(component.conversationId).toBeTruthy();
    expect(component.conversationId.length).toBeGreaterThan(0);
  });

  it('should initialize with empty chat history', () => {
    expect(component.chatHistory).toEqual([]);
  });

  it('should not send empty messages', () => {
    component.messageControl.setValue('');
    const initialLength = component.chatHistory.length;
    component.sendMessage();
    expect(component.chatHistory.length).toBe(initialLength);
  });
});
