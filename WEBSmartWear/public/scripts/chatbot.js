const chatbot = {
    apiKey: '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',
    // Chatbot için özel bağlam ve bilgi tabanı
    context: {
        store_info: {
            name: "SmartWear",
            specialties: ["giyim", "ayakkabı", "aksesuar", "kombin"],
            categories: {
                clothing: ["t-shirt", "gömlek", "pantolon", "ceket", "mont", "kazak", "hırka"],
                shoes: ["spor ayakkabı", "klasik ayakkabı", "bot", "sandalet"],
                accessories: ["saat", "kemer", "cüzdan", "çanta", "şapka", "atkı", "eldiven"],
            }
        },
        outfit_system: {
            description: "SmartWear'ın akıllı kombin oluşturma sistemi",
            how_it_works: [
                "1. Kullanıcılar ürün seçer ve sepete ekler",
                "2. Kombin Yap sayfasına giderek seçili ürünleri görür",
                "3. 'Kombinle' butonuna basarak eşleşen kombinleri bulur",
                "4. Sistem otomatik olarak uyumlu kombinleri listeler"
            ],
            season_system: {
                winter: "Bot, mont, kaban, parka, trençkot, palto türü ürünler → Kış kombinleri",
                spring: "Hırka, sweatshirt, ceket, yelek türü ürünler → Bahar kombinleri",
                special_codes: "CombinationCode 5 olan ürünler → Bahar kombinleri",
                kazak_category: "Subcategory 'kazak' olan ürünler → Bahar kombinleri",
                summer: "Diğer tüm ürünler → Yaz kombinleri"
            },
            features: [
                "Otomatik mevsim belirleme",
                "Eşleşen kombinler bulma",
                "Mevsime göre filtreleme (Kış/Bahar/Yaz)",
                "Toplam fiyat hesaplama",
                "Stok durumu kontrolü",
                "Tüm kombini sepete ekleme",
                "Ürün detaylarına gitme"
            ],
            tips: [
                "Farklı kategorilerden ürün seçerek daha fazla kombin seçeneği elde edebilirsiniz",
                "Seçili ürünlerden birini çıkararak yeni kombinler keşfedebilirsiniz",
                "Mevsim filtresini kullanarak istediğiniz döneme uygun kombinleri görebilirsiniz",
                "Kombinlerdeki ürünlere tıklayarak detaylarını inceleyebilirsiniz"
            ]
        },
        style_tips: [
            "Koyu renk pantolonlar ile açık renk üstler kombinlenebilir",
            "Spor ayakkabılar günlük kombinlerde tercih edilebilir",
            "Klasik ayakkabılar resmi görünüm için idealdir",
            "Aksesuarlar kombini tamamlayan en önemli parçalardır"
        ],
        size_guide: {
            clothing: "XS'ten 3XL'e kadar geniş beden aralığı",
            shoes: "36 numara ile 45 numara arası mevcut",
            measurement_tips: "Doğru beden için vücut ölçülerinizi alın"
        }
    },

    init() {
        this.createChatbotUI();
        this.attachEventListeners();
        // Hoşgeldin mesajı
        setTimeout(() => {
            this.addMessage('bot', 'Merhaba! Ben SmartWear Asistanı. Size nasıl yardımcı olabilirim?\n\nSorabileceğiniz konular:\n- Ürünler hakkında bilgi\n- Beden ve ölçü tavsiyeleri\n- Kombin önerileri ve Kombin Yap sayfası\n- Stil tavsiyeleri\n- Mevsimlik kombinler');
        }, 1000);
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
                <svg viewBox="0 0 24 24" class="chat-icon">
                    <path fill="currentColor" d="M12 1c-6.627 0-12 4.364-12 9.749 0 3.131 1.817 5.917 4.64 7.7.868 2.167-1.083 4.008-3.142 4.503 2.271.195 6.311-.121 9.374-2.498 7.095.538 13.128-3.997 13.128-9.705 0-5.385-5.373-9.749-12-9.749z"/>
                </svg>
                <svg viewBox="0 0 24 24" class="close-icon" style="display: none;">
                    <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
            </button>
        `;
        document.body.insertAdjacentHTML('beforeend', chatbotHTML);

        // Stil ekle
        const style = document.createElement('style');
        style.textContent = `
            .chatbot-toggle-button {
                position: fixed;
                bottom: 20px;
                right: 20px;
                width: 60px;
                height: 60px;
                border-radius: 30px;
                background: #0084ff;
                border: none;
                cursor: pointer;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                transition: all 0.3s ease;
                z-index: 1000;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 0;
            }

            .chatbot-toggle-button:hover {
                transform: scale(1.1);
                box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
            }

            .chatbot-toggle-button .chat-icon,
            .chatbot-toggle-button .close-icon {
                width: 30px;
                height: 30px;
                color: white;
            }

            .chatbot-toggle-button.active .chat-icon {
                display: none;
            }

            .chatbot-toggle-button.active .close-icon {
                display: block !important;
            }

            #chatbot-container {
                position: fixed;
                bottom: 90px;
                right: 20px;
                width: 350px;
                height: 500px;
                background: white;
                border-radius: 15px;
                box-shadow: 0 5px 25px rgba(0, 0, 0, 0.2);
                z-index: 999;
                overflow: hidden;
                transition: all 0.3s ease;
                display: flex;
                flex-direction: column;
            }

            .typing-indicator {
                display: flex;
                align-items: center;
                padding: 10px 15px;
                margin: 5px;
                background: #f0f0f0;
                border-radius: 15px;
                width: fit-content;
                max-width: 75%;
            }

            .typing-indicator span {
                height: 8px;
                width: 8px;
                background: #606060;
                border-radius: 50%;
                margin: 0 2px;
                display: inline-block;
                animation: bounce 1.3s linear infinite;
            }

            .typing-indicator span:nth-child(2) {
                animation-delay: 0.15s;
            }

            .typing-indicator span:nth-child(3) {
                animation-delay: 0.3s;
            }

            @keyframes bounce {
                0%, 60%, 100% {
                    transform: translateY(0);
                }
                30% {
                    transform: translateY(-4px);
                }
            }
        `;
        document.head.appendChild(style);
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

    showTypingIndicator() {
        const messagesContainer = document.getElementById('chatbot-messages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message bot typing-indicator';
        typingDiv.innerHTML = '<span></span><span></span><span></span>';
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return typingDiv;
    },

    removeTypingIndicator(indicator) {
        if (indicator && indicator.parentNode) {
            indicator.parentNode.removeChild(indicator);
        }
    },

    async sendMessage() {
        const input = document.getElementById('chatbot-input');
        const messagesContainer = document.getElementById('chatbot-messages');
        const userMessage = input.value.trim();

        if (!userMessage) return;

        // Kullanıcı mesajını ekle
        this.addMessage('user', userMessage);
        input.value = '';

        // Yazıyor... göstergesi
        const typingIndicator = this.showTypingIndicator();

        try {
            // Mevcut sayfayı kontrol et ve bağlamı ona göre ayarla
            const currentPage = window.location.pathname;
            let pageContext = "";
            
            if (currentPage.includes('clothing')) {
                pageContext = "Şu anda giyim kategorisi sayfasındasınız. ";
            } else if (currentPage.includes('shoes')) {
                pageContext = "Şu anda ayakkabı kategorisi sayfasındasınız. ";
            } else if (currentPage.includes('accessories')) {
                pageContext = "Şu anda aksesuar kategorisi sayfasındasınız. ";
            } else if (currentPage.includes('outfit')) {
                pageContext = "Şu anda Kombin Yap sayfasındasınız. Bu sayfada seçili ürünlerinizi görebilir, 'Kombinle' butonuna basarak eşleşen kombinleri bulabilirsiniz. Mevsim filtresi ile kombinleri kış/bahar/yaz olarak gruplandırabilirsiniz. ";
            }

            const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.apiKey}`,
                    'HTTP-Referer': window.location.origin
                },
                body: JSON.stringify({
                    model: 'openai/gpt-3.5-turbo',
                    messages: [
                        {
                            role: 'system',
                            content: `Sen SmartWear mağazasının yapay zeka asistanısın. Aşağıdaki bilgileri kullanarak müşterilere yardımcı ol:
                            ${pageContext}
                            
                            Mağaza Bilgileri: ${JSON.stringify(this.context.store_info)}
                            Kombin Sistemi: ${JSON.stringify(this.context.outfit_system)}
                            Stil Önerileri: ${JSON.stringify(this.context.style_tips)}
                            Beden Rehberi: ${JSON.stringify(this.context.size_guide)}
                            
                            Önemli Kurallar:
                            1. Her zaman Türkçe yanıt ver
                            2. Nazik ve yardımsever ol
                            3. Kombin Yap sayfası sorularında detaylı bilgi ver
                            4. Mevsim sistemini açıklarken öncelik sırasını belirt
                            5. Emin olmadığın konularda müşteriyi mağaza çalışanlarına yönlendir
                            6. Ürün fiyatları hakkında kesin bilgi verme, "... kategorisinde farklı fiyat seçenekleri mevcut" şeklinde yanıtla
                            7. Kombin önerilerinde mevsimi ve kullanım amacını dikkate al
                            8. Kombin Yap sayfasının özelliklerini ve nasıl kullanılacağını detaylıca anlat`
                        },
                        {
                            role: 'user',
                            content: userMessage
                        }
                    ]
                })
            });

            const data = await response.json();
            // Yazıyor... göstergesini kaldır
            this.removeTypingIndicator(typingIndicator);
            const botResponse = data.choices[0].message.content;
            this.addMessage('bot', botResponse);
        } catch (error) {
            console.error('API Hatası:', error);
            if (error.response) {
                console.error('API Yanıtı:', await error.response.json());
            }
            // Yazıyor... göstergesini kaldır
            this.removeTypingIndicator(typingIndicator);
            this.addMessage('bot', 'Üzgünüm, bir hata oluştu. Teknik ekibimiz bilgilendirildi. Lütfen birazdan tekrar deneyin.');
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