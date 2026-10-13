package code_sys.shopping_list.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import code_sys.shopping_list.R
import code_sys.shopping_list.databinding.FragmentOnboardingBinding
import code_sys.shopping_list.databinding.ItemOnboardingPageBinding

/** Данные одного обучающего слайда. */
data class OnboardingSlide(
    val illustration: Int,
    val titleRes: Int,
    val textRes: Int
)

/** Обучающие слайды, показываемые при первом запуске приложения. */
private val SLIDES = listOf(
    OnboardingSlide(R.drawable.ic_checklist, R.string.onboard_lists_title, R.string.onboard_lists_text),
    OnboardingSlide(R.drawable.ic_wheel, R.string.onboard_wheel_title, R.string.onboard_wheel_text),
    OnboardingSlide(R.drawable.ic_share, R.string.onboard_share_title, R.string.onboard_share_text)
)

/** Обучающий вход: знакомит с приложением при первом запуске. */
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pager.adapter = OnboardingPagerAdapter(this)
        TabLayoutMediator(binding.dots, binding.pager) { _, _ ->
            // Таб не имеет текста и работает просто точкой-индикатором.
        }.attach()

        binding.buttonSkip.setOnClickListener { finishOnboarding() }
        binding.buttonNext.setOnClickListener {
            val last = SLIDES.size - 1
            if (binding.pager.currentItem < last) {
                binding.pager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        // На последнем слайде кнопка превращается в «Начать».
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.buttonNext.setText(
                    if (position == SLIDES.size - 1) R.string.onboarding_start else R.string.onboarding_next
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Обучающий экран — без панели действий.
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onPause() {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        super.onPause()
    }

    /** Помечает обучение пройденным и открывает главный экран. */
    private fun finishOnboarding() {
        OnboardingPrefs.markCompleted(requireContext())
        findNavController().navigate(
            R.id.listsFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.onboardingFragment, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Листалка обучающих экранов. */
class OnboardingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = SLIDES.size

    override fun createFragment(position: Int): Fragment =
        OnboardingPageFragment.newInstance(position)
}

/** Один слайд обучения. */
class OnboardingPageFragment : Fragment() {

    private var _pageBinding: ItemOnboardingPageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _pageBinding = ItemOnboardingPageBinding.inflate(inflater, container, false)
        return _pageBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val slide = SLIDES[arguments?.getInt(ARG_POSITION) ?: 0]
        val b = _pageBinding!!
        b.illustration.setImageResource(slide.illustration)
        b.title.setText(slide.titleRes)
        b.description.setText(slide.textRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _pageBinding = null
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingPageFragment =
            OnboardingPageFragment().apply {
                arguments = Bundle().apply { putInt(ARG_POSITION, position) }
            }
    }
}

/** Хранилище флага «онбординг уже пройден». */
object OnboardingPrefs {
    private const val PREFS_NAME = "shopping_prefs"
    private const val KEY_COMPLETED = "onboarding_completed_v1"

    fun isCompleted(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETED, false)

    fun markCompleted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_COMPLETED, true).apply()
    }
}
