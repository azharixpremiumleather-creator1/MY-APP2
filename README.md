# AZHARIX Marketplace — VS Code Starter

A responsive, premium-looking marketplace front-end inspired by large multi-seller shopping sites. It includes:
- Black, gold, white and blue visual theme
- 1,000 generated product listings with USD prices above $100
- Product search, categories, price filters, sorting and pagination
- Demo buyer sign-up/sign-in
- Cart, saved/favourite products and demo checkout
- Seller registration concept with a $43 USD fee display
- Demo seller dashboard and add-listing interaction
- Responsive desktop and mobile layouts

## Run in Visual Studio Code

1. Download and unzip `AZHARIX-VSCode.zip`.
2. Open VS Code.
3. Select **File → Open Folder…** and choose the unzipped `AZHARIX` folder.
4. Open `index.html`.
5. Recommended: install the **Live Server** extension (by Ritwick Dey), right-click `index.html`, and select **Open with Live Server**. Or double-click `index.html` to open it in your browser.

## Important: demo vs real marketplace

This is a **front-end prototype**, not a production marketplace. The 1,000 products are generated in `app.js` and are sample/demo listings, not actual inventory. Product images are stylized emoji/CSS visuals. Demo account information and cart are stored in the current browser's local storage.

The $43 seller fee is displayed and simulated only: **no payment is collected**. Authentication is not secure, listings are not shared between different users/devices, and checkout does not take payment. Do not use this prototype to collect real passwords, payment details, or orders.

## To launch for real buyers and sellers

A production version needs:
- A backend API and database (for users, products, carts, orders and seller profiles)
- Secure authentication, password hashing, email verification and account recovery
- A payment provider such as Stripe or a supported local provider, with server-side payment verification for the $43 seller registration
- Seller identity/business verification, moderation and anti-fraud measures
- Real product photos, inventory management, shipping, taxes, refunds, privacy policy and terms
- Buyer/seller messaging, reviews, dispute handling and admin dashboard
- HTTPS, logging, rate limits, backups and security testing

Never store raw passwords or payment card details in browser local storage. A real $43 fee must be processed and verified server-side before activating a seller account.
