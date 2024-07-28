document.addEventListener('DOMContentLoaded', () => {
    const dropdownBtns = document.querySelectorAll('.dropdown-btn');
    console.log(dropdownBtns);
    dropdownBtns.forEach((dropdownBtn) => {
        dropdownBtn.addEventListener('click', (event) => {
            event.preventDefault();

            const dropdownContent = dropdownBtn.nextElementSibling;

            // Close all other dropdowns
            closeAllDropdowns();

            if (dropdownContent && dropdownContent.classList.contains('dropdown-content')) {
                dropdownContent.classList.toggle('animate');
                dropdownContent.style.display = 'block';
            }
        });
    });

    function closeAllDropdowns() {
        const dropdownContents = document.querySelectorAll('.dropdown-content');
        dropdownContents.forEach((dropdownContent) => {
            dropdownContent.classList.remove('animate');
            dropdownContent.style.display = 'none';
        });
    }

    document.addEventListener('click', (event) => {
        const target = event.target;

        // Close dropdowns if clicked outside
        if (!target.classList.contains('dropdown-btn')) {
            closeAllDropdowns();
        }
    });
});

