package live.hails.hailsdotgo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val HailsDotGoTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,    fontSize = 57.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 22.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 16.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 16.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 14.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 11.sp),
)
