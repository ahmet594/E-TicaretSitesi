// Set up category filters
function setupCategoryFilters() {
    // Select all category filter buttons
    const categoryButtons = document.querySelectorAll('.category-filter');
    
    // Set the initial active category based on the currentCategory value
    categoryButtons.forEach(button => {
        // For 'all' category, use 'all' data attribute
        if (button.dataset.category === 'all' && currentCategory === 'all') {
            button.classList.add('active');
        }
        // For specific categories, match the button's data-category with currentCategory
        else if (button.dataset.category === currentCategory) {
            button.classList.add('active');
        }
    });
    
    // Add click event listener to each button
    categoryButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove active class from all buttons
            categoryButtons.forEach(btn => btn.classList.remove('active'));
            
            // Add active class to clicked button
            button.classList.add('active');
            
            // Get the category from button's data attribute
            const category = button.dataset.category;
            
            // Load products based on selected category
            if (category === 'all') {
                currentCategory = 'all';
                loadAllProducts();
            } else {
                currentCategory = category;
                loadProductsByCategory(category);
            }
        });
    });
}

// Activate the category link in navbar based on current page
function activateCurrentCategoryLink() {
    const currentPath = window.location.pathname;
    console.log("Current path:", currentPath);
    
    const categoryLinks = document.querySelectorAll('.category-nav-link');
    
    categoryLinks.forEach(link => {
        const linkHref = link.getAttribute('href');
        console.log("Checking link:", linkHref);
        
        link.classList.remove('active');
        
        if (currentPath.includes('women.html') && linkHref.includes('women.html')) {
            link.classList.add('active');
            console.log("Women page active");
        }
        else if (currentPath.includes('men.html') && linkHref.includes('men.html')) {
            link.classList.add('active');
            console.log("Men page active");
        }
    });
} 