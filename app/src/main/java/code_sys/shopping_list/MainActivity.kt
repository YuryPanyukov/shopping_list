package code_sys.shopping_list

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import code_sys.shopping_list.data.ShoppingRepository
import code_sys.shopping_list.databinding.ActivityMainBinding
import code_sys.shopping_list.ui.OnboardingPrefs

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ShoppingRepository.init(applicationContext)

        // Material You: с Android 12 цвета берутся из обоев, ниже применяется
        // фирменная палитра из тем приложения.
        DynamicColors.applyToActivitiesIfAvailable(application)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Клавиатура тоже отступ: иначе поле ввода и кнопки уезжают под неё.
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.listsFragment)).build()
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Обучающий вход: показываем только при первом запуске.
        if (savedInstanceState == null && !OnboardingPrefs.isCompleted(this)) {
            navController.navigate(R.id.onboardingFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
