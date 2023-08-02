package com.Kelompok_16.myapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import coil.load
import com.Kelompok_16.myapplication.R
import com.Kelompok_16.myapplication.data.TouristsDataSource
import com.Kelompok_16.myapplication.databinding.FragmentHomeBinding
import com.Kelompok_16.myapplication.databinding.ItemCategoryBinding
import com.Kelompok_16.myapplication.databinding.ItemHeadlineBinding
import com.Kelompok_16.myapplication.model.TouristAttraction
import com.Kelompok_16.myapplication.ui.SharedViewModel
import com.Kelompok_16.myapplication.ui.detail.DetailActivity
import com.Kelompok_16.myapplication.utils.resolveAttributeColor
import com.google.android.gms.maps.model.LatLng
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class HomeFragment : Fragment() {

    /* Buat property _binding, dengan tipe FragmentHomeBinding? dan inisialisasi null */
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var currentLocation: LatLng? = LatLng(-7.05800729, 109.426030)


    /* Buat property headlineData, dengan tipe List<TouristAttraction> dan inisialisasi dengan memanggil TouristsDataSource.createHeadlineData()
    * Inisialisasi viewModel dengan memanggil activityViewModels() tujuannya agar viewModel dapat digunakan di fragment ini
    *  */
    private val headlineData = TouristsDataSource.createTouristAttractionData().take(5)
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        initializeCategory()
        setupCarouselHeadline()
        observeLivedata()

        return binding.root
    }

    /* Buat function observeLivedata() untuk mengamati livedata dari viewModel.location
    * Setelah itu, jika livedata tidak null, maka kita akan mengubah text dari tvLocation dengan memanggil viewModel.getStringLocation()
     */
    private fun observeLivedata() { // buat live location yang di Pemalang
        viewModel.location.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.tvLocation.text = viewModel.getStringLocation(requireContext(), it)
                currentLocation = it
            } else {
                currentLocation = LatLng(-7.05800729, 109.426030)
            }
        }

        viewModel.touristAttractionData.observe(viewLifecycleOwner) {
            setupFootNote(it)
        }
    }

    private fun setupCarouselHeadline() { //buat Carousel (yang geser geser ) diatas supaay tetap looping
        with(binding.carouselHeadline) {
            registerLifecycle(lifecycle)
            carouselListener = object : CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup
                ): ViewBinding {
                    return ItemHeadlineBinding.inflate(layoutInflater, parent, false)
                }

                override fun onBindViewHolder(
                    binding: ViewBinding,
                    item: CarouselItem,
                    position: Int
                ) {
                    val currentBinding = binding as ItemHeadlineBinding
                    val touristAttraction = headlineData[position]

                    with(currentBinding) {
                        tvTitle.text = touristAttraction.name
                        tvSubtitle.text = touristAttraction.locationName
                        ivHeadline.load(touristAttraction.imageUrl)
                        root.setOnClickListener {
                            navigateToDetail(touristAttraction)
                        }
                    }
                }
            }

            val listFour = headlineData.map { touristAttraction ->
                CarouselItem(imageUrl = touristAttraction.imageUrl)
            }

            setData(listFour)
            setIndicator(binding.customIndicator)
        }

    }

    /* Buat function setupFootNote() untuk mengatur adapter dari rvFootnote
    * Buat object footNoteAdapter dengan tipe HomeFooterAdapter dan inisialisasi dengan memanggil HomeFooterAdapter(headlineData)
    * Setelah itu, kita akan mengatur rvFootnote dengan memanggil footNoteAdapter
     */
    private fun setupFootNote(data: List<TouristAttraction>) { // buaat titik dibawah gambar (dibawah carousel)
        val footNoteAdapter = HomeFooterAdapter(data)
        with(binding.rvFootnote) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = footNoteAdapter.apply {
                onItemClick = { touristAttraction ->
                    navigateToDetail(touristAttraction)
                }
            }
            setHasFixedSize(true)
        }
    }

    private fun initializeCategory() { // buat category dan ketika kategori di klik
        with(binding) {
            setupCategory(ivCategoryBeach, "Pantai", R.drawable.ic_category_beach)
            setupCategory(ivCategoryLake, "Danau", R.drawable.ic_category_lake)
            setupCategory(ivCategoryHill, "Bukit", R.drawable.ic_category_hill)
            setupCategory(ivCategoryPopular, "Popular", R.drawable.ic_category_popular)

            activatedCategory(ivCategoryPopular) // fungsi ketika didalam kategori

            ivCategoryPopular.root.setOnClickListener { activatedCategory(ivCategoryPopular) }
            ivCategoryBeach.root.setOnClickListener { activatedCategory(ivCategoryBeach) }
            ivCategoryLake.root.setOnClickListener { activatedCategory(ivCategoryLake) }
            ivCategoryHill.root.setOnClickListener { activatedCategory(ivCategoryHill) }
        }
    }

    private fun setupCategory( // fungsi buat setup category
        binding: ItemCategoryBinding,
        categoryName: String,
        categoryImage: Int,
    ) {
        with(binding) {
            tvCategory.text = categoryName
            ivCategory.setImageResource(categoryImage)
        }
    }

    private fun clearAllCategory() { // fungsi buat clear category agar menampilkan data baru
        with(binding) {
            clearCategory(ivCategoryBeach)
            clearCategory(ivCategoryLake)
            clearCategory(ivCategoryHill)
            clearCategory(ivCategoryPopular)
        }
    }

    private fun clearCategory( // untuk membalikkan posisi kategori setelah kita klik
        binding: ItemCategoryBinding,
    ) {
        with(binding) {
            val colorSurface =
                requireContext().resolveAttributeColor(android.R.attr.colorBackground)

            root.strokeColor = ContextCompat.getColor(requireContext(), R.color.alto)
            root.setCardBackgroundColor(colorSurface)
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_gray))
            ivCategory.setColorFilter(ContextCompat.getColor(requireContext(), R.color.slate_gray))
        }
    }

    private fun activatedCategory( // umtuk memberikan warna pada kategori yang aktif
        binding: ItemCategoryBinding,
    ) {
        clearAllCategory()
        with(binding) {
            root.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            ivCategory.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

            viewModel.filterTouristAttraction(tvCategory.text.toString())
        }
    }

    private fun navigateToDetail(item: TouristAttraction) {// navigasi untuk detail screen
        currentLocation?.let {
            val intent = Intent(
                requireContext(),
                DetailActivity::class.java
            ).putExtra(DetailActivity.EXTRA_TOURIST_ATTRACTION, item)
                .putExtra(DetailActivity.EXTRA_LATITUDE, it.latitude)
                .putExtra(DetailActivity.EXTRA_LONGITUDE, it.longitude)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
