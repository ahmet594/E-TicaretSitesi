// Alt kategori seçenekleri
const subcategoryOptions = {
    'Giyim': [
        't-shirt', 'gömlek', 'kazak', 'hırka', 'sweatshirt', 
        'mont', 'kaban-parka', 'ceket', 'kot-pantolon', 
        'kumaş-pantolon', 'şortlar', 'takım-elbise', 'eşofman', 'yelek'
    ],
    'Ayakkabı': [
        'Spor Ayakkabı', 'Sneaker', 'Bot', 'Klasik'
    ],
    'Aksesuar': [
        'Kemer', 'Kravat', 'Şapka', 'Atkı', 'Eldiven', 'Saat', 'Cüzdan'
    ]
};

// DOM elementleri
const addProductForm = document.getElementById('add-product-form');
const categorySelect = document.getElementById('product-category');
const subcategorySelect = document.getElementById('product-subcategory');
const imageInput = document.getElementById('product-image');
const imagePreview = document.getElementById('image-preview');

// Sayfa yüklendiğinde
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
});

// Event listener'ları ayarla
function setupEventListeners() {
    // Kategori değiştiğinde alt kategorileri güncelle
    categorySelect.addEventListener('change', () => {
        updateSubcategoryOptions(categorySelect.value);
    });

    // Görsel önizleme
    imageInput.addEventListener('change', handleImagePreview);

    // Form gönderimi
    addProductForm.addEventListener('submit', handleProductSubmit);
}

// Alt kategori seçeneklerini güncelle
function updateSubcategoryOptions(category) {
    subcategorySelect.innerHTML = '<option value="">Alt Kategori Seçin</option>';

    if (subcategoryOptions[category]) {
        subcategoryOptions[category].forEach(subcategory => {
            const option = document.createElement('option');
            option.value = subcategory;
            option.textContent = subcategory;
            subcategorySelect.appendChild(option);
        });
    }
}

// Görsel önizleme
function handleImagePreview(e) {
    const file = e.target.files[0];
    if (!file) return;

    // Dosya tipini kontrol et
    if (!file.type.startsWith('image/')) {
        alert('Lütfen geçerli bir görsel dosyası seçin');
        imageInput.value = '';
        return;
    }

    // Dosya boyutunu kontrol et (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
        alert('Görsel boyutu 5MB\'dan küçük olmalıdır');
        imageInput.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        imagePreview.innerHTML = `
            <div class="image-preview-item">
                <img src="${e.target.result}" alt="Önizleme">
                <button type="button" class="remove-image" onclick="removeImage()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
    };
    reader.readAsDataURL(file);
}

// Görseli kaldır
function removeImage() {
    imageInput.value = '';
    imagePreview.innerHTML = '';
}

// Ürün ekleme
async function handleProductSubmit(e) {
    e.preventDefault();

    const formData = new FormData(addProductForm);
    const productData = Object.fromEntries(formData.entries());

    // Görsel dosyasını ekle
    const imageFile = imageInput.files[0];
    if (imageFile) {
        const imageData = new FormData();
        imageData.append('image', imageFile);

        try {
            // Önce görseli yükle
            const imageResponse = await fetch('/api/upload', {
                method: 'POST',
                body: imageData
            });

            if (!imageResponse.ok) {
                throw new Error('Görsel yüklenirken bir hata oluştu');
            }

            const imageResult = await imageResponse.json();
            productData.image = imageResult.imagePath;
        } catch (error) {
            console.error('Görsel yüklenirken hata:', error);
            alert('Görsel yüklenirken bir hata oluştu');
            return;
        }
    }

    try {
        // Ürünü ekle
        const response = await fetch('/api/products', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(productData)
        });

        if (!response.ok) {
            throw new Error('Ürün eklenirken bir hata oluştu');
        }

        alert('Ürün başarıyla eklendi');
        addProductForm.reset();
        imagePreview.innerHTML = '';
        window.location.href = '/admin/manage-products.html';
    } catch (error) {
        console.error('Ürün eklenirken hata:', error);
        alert('Ürün eklenirken bir hata oluştu');
    }
} 