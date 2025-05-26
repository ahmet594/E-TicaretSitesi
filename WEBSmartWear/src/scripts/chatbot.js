const chatbot = {
    apiKey: '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',
    init() {
        this.createChatbotUI();
        this.attachEventListeners();
    },

    createChatbotUI() {
        const chatbotHTML = `
            <div id="chatbot-container" class="chatbot-hidden">
                <div id="chatbot-header">
                    <h3>SmartWear Asistan</h3>
                    <button id="close-chatbot">×</button>
                </div>
                <div id="chatbot-messages"></div>
                <div id="chatbot-input-container">
                    <input type="text" id="chatbot-input" placeholder="Mesajınızı yazın...">
                    <button id="send-message">Gönder</button>
                </div>
            </div>
            <button id="chatbot-toggle" class="chatbot-toggle-button">
                <span class="chat-icon">💬</span>
            </button>
        `;
        document.body.insertAdjacentHTML('beforeend', chatbotHTML);
    },

    attachEventListeners() {
        const toggle = document.getElementById('chatbot-toggle');
        const container = document.getElementById('chatbot-container');
        const closeBtn = document.getElementById('close-chatbot');
        const sendBtn = document.getElementById('send-message');
        const input = document.getElementById('chatbot-input');

        toggle.addEventListener('click', () => {
            container.classList.toggle('chatbot-hidden');
            toggle.classList.toggle('active');
        });

        closeBtn.addEventListener('click', () => {
            container.classList.add('chatbot-hidden');
            toggle.classList.remove('active');
        });

        sendBtn.addEventListener('click', () => this.sendMessage());
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    },

    async sendMessage() {
        const input = document.getElementById('chatbot-input');
        const messagesContainer = document.getElementById('chatbot-messages');
        const userMessage = input.value.trim();

        if (!userMessage) return;

        // Kullanıcı mesajını ekle
        this.addMessage('user', userMessage);
        input.value = '';

        try {
            const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.apiKey}`,
                    'HTTP-Referer': window.location.origin,
                    'X-Title': 'SmartWear Chatbot'
                },
                body: JSON.stringify({
                    model: 'google/gemma-3n-e4b-it:free',
                    messages: [
                        {
                            role: 'user',
                            content: userMessage
                        }
                    ]
                })
            });

            const data = await response.json();
            const botResponse = data.choices[0].message.content;
            this.addMessage('bot', botResponse);
        } catch (error) {
            console.error('Error:', error);
            this.addMessage('bot', 'Üzgünüm, bir hata oluştu. Lütfen tekrar deneyin.');
        }
    },

    addMessage(sender, message) {
        const messagesContainer = document.getElementById('chatbot-messages');
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', sender);
        messageElement.textContent = message;
        messagesContainer.appendChild(messageElement);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
};

document.addEventListener('DOMContentLoaded', () => chatbot.init()); 