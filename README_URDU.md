# AZHARIX College Management System — Android Starter

یہ Android Studio کے لیے ایک ابتدائی Kotlin + Jetpack Compose پروجیکٹ ہے۔

## کیا موجود ہے؟
- AZHARIX برانڈنگ کے ساتھ لاگ اِن اسکرین
- ڈیش بورڈ اور سائیڈ نیویگیشن
- پروفائل، نتائج، حاضری، فیس، ٹائم ٹیبل، نوٹس اور سیٹنگز کے نمونہ صفحات
- نیلا/سفید ڈیزائن

## اہم وضاحت
یہ ابھی **فرنٹ اینڈ ڈیمو** ہے، مکمل محفوظ کالج سسٹم نہیں۔ لاگ اِن بٹن اصل اکاؤنٹ کی تصدیق نہیں کرتا۔ نمونہ نمبر، حاضری اور فیس حقیقی ریکارڈ نہیں ہیں۔ اسے حقیقی طلبہ کے ڈیٹا یا ادائیگیوں کے لیے استعمال نہ کریں جب تک محفوظ بیک اینڈ، ڈیٹا بیس، اجازتوں اور ٹیسٹنگ کا کام مکمل نہ ہو۔

## چلانے کا طریقہ
1. کمپیوٹر پر Android Studio انسٹال کریں: https://developer.android.com/studio
2. ZIP فائل Extract کریں۔
3. Android Studio میں **Open** منتخب کریں اور Extract کیا ہوا `AZHARIX_Android_Starter` فولڈر کھولیں۔
4. Gradle Sync مکمل ہونے دیں۔ پہلی بار انٹرنیٹ درکار ہو سکتا ہے تاکہ Gradle اور Android libraries ڈاؤن لوڈ ہوں۔
5. Android فون پر Developer Options اور USB debugging فعال کریں، فون USB سے جوڑیں؛ یا Android Studio میں Device Manager سے Emulator بنائیں۔
6. اوپر موجود Run ▶ بٹن دبائیں۔
7. APK بنانے کے لیے: **Build > Build Bundle(s) / APK(s) > Build APK(s)**۔ فائل عموماً `app/build/outputs/apk/debug/app-debug.apk` میں بنتی ہے۔

## اگلا مرحلہ
پروڈکشن کے لیے Firebase Authentication یا کسی محفوظ backend، Firestore/SQL database، role-based authorization، server-side validation، backup اور privacy policy کی ضرورت ہوگی۔ Google Play پر شائع کرنے سے پہلے موجودہ Play Console کی تمام شرائط اور ٹیسٹنگ تقاضے چیک کریں۔
