# Jetpack Compose Migration Guide

This guide provides practical steps to migrate OpenTransition from XML layouts to Jetpack Compose.

## Quick Start: Proof of Concept

Before committing to a full migration, create a proof of concept by migrating one simple screen.

### 1. Add Compose Dependencies

Update `mobile/build.gradle`:

```gradle
android {
    // ... existing config ...
    
    buildFeatures {
        viewBinding true
        compose true  // Add this
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"  // Match Kotlin version
    }
}

dependencies {
    // ... existing dependencies ...
    
    // Jetpack Compose BOM
    def composeBom = platform('androidx.compose:compose-bom:2024.12.00')
    implementation composeBom
    androidTestImplementation composeBom
    
    // Compose
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.9.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    implementation 'androidx.navigation:navigation-compose:2.8.5'
    
    // Compose + existing libraries
    implementation 'androidx.compose.runtime:runtime-rxjava3'  // RxJava integration
    
    // Debug tools
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
    
    // Testing
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
}
```

### 2. Create Material 3 Theme

Create `mobile/src/main/java/com/shelbeely/opentransition/ui/theme/Theme.kt`:

```kotlin
package com.shelbeely.opentransition.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    // ... add other colors
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    // ... add other colors
)

@Composable
fun OpenTransitionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

Create `mobile/src/main/java/com/shelbeely/opentransition/ui/theme/Type.kt`:

```kotlin
package com.shelbeely.opentransition.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 3. Migrate a Simple Screen (Lock Screen Example)

The lock screen is a good candidate for the first migration as it's simple and self-contained.

**Before: `LockFragment.kt` (using ViewBinding)**

```kotlin
class LockFragment : Fragment() {
    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }
    
    private fun setupUI() {
        binding.unlockButton.setOnClickListener {
            // Handle unlock
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**After: `LockScreen.kt` (using Compose)**

```kotlin
package com.shelbeely.opentransition.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme

@Composable
fun LockScreen(
    viewModel: LockViewModel = viewModel(),
    onUnlocked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LockScreenContent(
        uiState = uiState,
        onPinChanged = viewModel::onPinChanged,
        onUnlockClicked = {
            if (viewModel.verifyPin()) {
                onUnlocked()
            }
        }
    )
}

@Composable
private fun LockScreenContent(
    uiState: LockUiState,
    onPinChanged: (String) -> Unit,
    onUnlockClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = uiState.pin,
                onValueChange = onPinChanged,
                label = { Text("PIN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onUnlockClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.pin.isNotEmpty()
            ) {
                Text("Unlock")
            }
        }
    }
}

data class LockUiState(
    val pin: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    OpenTransitionTheme {
        LockScreenContent(
            uiState = LockUiState(),
            onPinChanged = {},
            onUnlockClicked = {}
        )
    }
}
```

**Update ViewModel to use StateFlow:**

```kotlin
class LockViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()
    
    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin, error = null) }
    }
    
    fun verifyPin(): Boolean {
        val isValid = // ... your verification logic
        if (!isValid) {
            _uiState.update { it.copy(error = "Invalid PIN") }
        }
        return isValid
    }
}
```

### 4. Host Compose in Activity/Fragment

You can embed Compose screens in existing Activities:

```kotlin
class LockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            OpenTransitionTheme {
                LockScreen(
                    onUnlocked = {
                        // Navigate or finish
                        finish()
                    }
                )
            }
        }
    }
}
```

Or in a Fragment (for incremental migration):

```kotlin
class LockFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OpenTransitionTheme {
                    LockScreen(
                        onUnlocked = {
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }
    }
}
```

## Migration Strategy

### Phase 1: Simple Screens (Week 1-2)
Migrate screens with simple UI and minimal state:
- ✅ Lock screen
- Settings sub-screens (About, Privacy Policy, etc.)
- Splash screen (if exists)

**Benefits:**
- Learn Compose patterns
- Establish team conventions
- Build confidence

### Phase 2: List Screens (Week 3-4)
Migrate screens with lists:
- Milestones list
- Settings main screen
- Photo type selection

**Key Learnings:**
- `LazyColumn` vs `RecyclerView`
- Item animations
- Swipe actions
- Pull to refresh

### Phase 3: Complex Forms (Week 5-6)
Migrate screens with forms and validation:
- Add/Edit Milestone
- Settings forms

**Key Learnings:**
- Form state management
- Validation
- Keyboard handling
- Focus management

### Phase 4: Media Screens (Week 7-9)
Migrate photo and media screens:
- Gallery grid
- Single photo view
- Photo selection

**Key Learnings:**
- Image loading (Coil/Glide with Compose)
- Gestures (zoom, pan)
- Custom layouts
- Performance optimization

### Phase 5: Camera & Advanced (Week 10-12)
Migrate complex screens:
- Camera screen
- Photo editor
- Home dashboard

**Key Learnings:**
- `AndroidView` for CameraX
- Complex gestures
- Canvas drawing
- Performance critical UIs

### Phase 6: Navigation (Week 13-14)
Migrate to Compose Navigation:
- Set up Navigation Graph
- Update deep links
- Handle arguments

### Phase 7: Wear OS (Week 15-16)
Migrate Wear OS app to Compose for Wear:
- Use `androidx.wear.compose`
- Maintain Wearable Data Layer
- Test cross-device communication

## Common Patterns

### Displaying Images

Using Coil (recommended for Compose):

```kotlin
// Add dependency
implementation("io.coil-kt:coil-compose:2.5.0")

// Use in Composable
@Composable
fun PhotoItem(photoUrl: String) {
    AsyncImage(
        model = photoUrl,
        contentDescription = "Photo",
        modifier = Modifier.size(100.dp),
        contentScale = ContentScale.Crop
    )
}
```

### Lists with RecyclerView-like Features

```kotlin
@Composable
fun MilestoneList(
    milestones: List<Milestone>,
    onMilestoneClick: (Milestone) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = milestones,
            key = { it.id }  // Important for animations
        ) { milestone ->
            MilestoneCard(
                milestone = milestone,
                onClick = { onMilestoneClick(milestone) },
                modifier = Modifier.animateItemPlacement()  // Smooth animations
            )
        }
    }
}
```

### Side Effects (like LiveData observers)

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    // Collect Flow
    val uiState by viewModel.uiState.collectAsState()
    
    // One-time effects
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    // Effect that depends on a value
    LaunchedEffect(uiState.shouldShowSnackbar) {
        if (uiState.shouldShowSnackbar) {
            // Show snackbar
        }
    }
    
    // UI based on state
    MyScreenContent(uiState)
}
```

### Navigation

```kotlin
// Set up NavHost
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToCamera = {
                    navController.navigate("camera")
                }
            )
        }
        
        composable("camera") {
            CameraScreen(
                onPhotoTaken = { photoUri ->
                    navController.navigate("photo/$photoUri")
                }
            )
        }
        
        composable(
            route = "photo/{photoUri}",
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoUri = backStackEntry.arguments?.getString("photoUri")
            PhotoScreen(photoUri = photoUri)
        }
    }
}
```

## Testing Compose UI

```kotlin
@RunWith(AndroidJUnit4::class)
class LockScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun lockScreen_displaysCorrectly() {
        composeTestRule.setContent {
            OpenTransitionTheme {
                LockScreen(onUnlocked = {})
            }
        }
        
        composeTestRule.onNodeWithText("Enter PIN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unlock").assertIsDisplayed()
    }
    
    @Test
    fun lockScreen_enterPin_enablesButton() {
        composeTestRule.setContent {
            OpenTransitionTheme {
                LockScreen(onUnlocked = {})
            }
        }
        
        composeTestRule.onNodeWithText("Unlock").assertIsNotEnabled()
        composeTestRule.onNodeWithText("PIN").performTextInput("1234")
        composeTestRule.onNodeWithText("Unlock").assertIsEnabled()
    }
}
```

## Performance Tips

1. **Remember Expensive Computations:**
```kotlin
@Composable
fun ExpensiveScreen(data: List<Item>) {
    val processedData = remember(data) {
        data.map { /* expensive operation */ }
    }
}
```

2. **Use derivedStateOf for Computed State:**
```kotlin
@Composable
fun MyScreen(items: List<Item>) {
    val filteredItems by remember {
        derivedStateOf {
            items.filter { it.isActive }
        }
    }
}
```

3. **Stable Parameters:**
```kotlin
// Mark data classes as @Immutable or @Stable
@Immutable
data class Milestone(val id: String, val title: String)
```

4. **Lazy Loading:**
```kotlin
@Composable
fun LazyList() {
    LazyColumn {
        items(10000) { index ->
            Text("Item $index")  // Only visible items are composed
        }
    }
}
```

## Resources

- [Compose Documentation](https://developer.android.com/jetpack/compose)
- [Compose Samples](https://github.com/android/compose-samples)
- [Migration Guide](https://developer.android.com/jetpack/compose/migrate)
- [Material 3 Components](https://m3.material.io/)
- [Compose Layouts](https://developer.android.com/jetpack/compose/layouts)
- [State Management](https://developer.android.com/jetpack/compose/state)

## Next Steps

1. ✅ Complete proof of concept (Lock screen)
2. Get team feedback on approach
3. Establish coding standards for Compose
4. Set up CI for Compose (screenshot tests, etc.)
5. Begin incremental migration following the phase plan
6. Update documentation as you go
7. Remove XML layouts after Compose migration is complete
