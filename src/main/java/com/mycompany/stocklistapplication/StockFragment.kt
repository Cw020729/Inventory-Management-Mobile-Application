package com.mycompany.stocklistapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.util.Date
import java.util.UUID

private const val ARG_STOCK_ID = "stock_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private const val REQUEST_IMAGE_PICKER = 2

private const val DATE_FORMAT = "EEE, MM, dd"
private const val ARG_IMAGE_URL = "image_url"


class StockFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks  {

    private lateinit var stock: Stock
    private lateinit var deleteButton: ImageButton
    private lateinit var itemName: EditText
    private lateinit var itemManufacture: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemQuantity: EditText
    private lateinit var plusButton: Button
    private lateinit var minusButton: Button
    private lateinit var dateButton: Button
    private lateinit var saveButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var itemImage: ImageView
    private var imageUrl: String? = null
    var isNewStock: Boolean = false
    var isDateChanged: Boolean = true

    private val stockDetailViewModel: StockDetailViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(StockDetailViewModel::class.java)
    }

    companion object {
        fun newInstance(stockId: UUID?): StockFragment {
            val args = Bundle().apply {
                putSerializable(ARG_STOCK_ID, stockId)
            }
            return StockFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stockId: UUID? = arguments?.getSerializable(ARG_STOCK_ID) as? UUID
        if (savedInstanceState != null) {
            imageUrl = savedInstanceState.getString(ARG_IMAGE_URL)
        } else {
            imageUrl = null
        }
        if (stockId != null) {
            stock = Stock()
            stockDetailViewModel.loadStock(stockId)
            isNewStock = false
            isDateChanged = true
        } else {
            stock = Stock()
            isNewStock = true
            isDateChanged = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragement_stock, container, false)
        deleteButton = view.findViewById(R.id.deleteItemButton)
        itemName = view.findViewById(R.id.itemName)
        itemManufacture = view.findViewById(R.id.itemManufacture)
        itemPrice = view.findViewById(R.id.itemPrice)
        itemQuantity = view.findViewById(R.id.itemQuantity)
        plusButton = view.findViewById(R.id.plusButton)
        minusButton = view.findViewById(R.id.minusButton)
        dateButton = view.findViewById(R.id.dateButton)
        saveButton = view.findViewById(R.id.saveButton)
        itemImage = view.findViewById(R.id.itemImage)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        if (isNewStock) {
            deleteButton.visibility = View.GONE // Hide the delete button for new stocks
        } else {
            deleteButton.visibility = View.VISIBLE // Show the delete button for existing stocks
        }
        selectImageButton.setOnClickListener {
            openImagePicker()
        }
        savedInstanceState?.let {
            imageUrl = it.getString(ARG_IMAGE_URL)
        }

        plusButton.setOnClickListener {
            updateQuantity(1)
        }

        minusButton.setOnClickListener {
            updateQuantity(-1)
        }
        dateButton.setOnClickListener {
            val newName = itemName.text.toString()
            val newManufacture = itemManufacture.text.toString()
            val newPrice = itemPrice.text.toString().toDoubleOrNull() ?: 0.0
            val newQuantity = itemQuantity.text.toString().toIntOrNull() ?: 0

            stock.name = newName
            stock.manufacture = newManufacture
            stock.costPerItem = newPrice
            stock.quantity = newQuantity
            isDateChanged = true
            DatePickerFragment.newInstance(stock.restockDate).apply {
                setTargetFragment(this@StockFragment, REQUEST_DATE)
                show(this@StockFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        saveButton.setOnClickListener {
            if(allDataEntered()) {
                saveStock()
            }
        }
        deleteButton.setOnClickListener {
            deleteStock()
        }

        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stockDetailViewModel.stockLiveData.observe(
            viewLifecycleOwner
        ) { stock ->
            stock?.let {
                this.stock = stock
                updateUI()
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back button press here
                loadMainActivity()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun updateUI() {
        itemName.setText(stock.name)
        itemManufacture.setText(stock.manufacture)
        itemPrice.setText(stock.costPerItem.toString())
        itemQuantity.setText(stock.quantity.toString())
        dateButton.text = stock.restockDate.toString()
        if(stock.itemPicture != ""){
        loadItemImageFromUrl(stock.itemPicture)
        }


    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the selected image URI in the instance state
        outState.putString(ARG_IMAGE_URL, imageUrl)
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                stock.name = sequence.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        itemName.addTextChangedListener(titleWatcher)
    }

    private fun saveStock() {
        val newName = itemName.text.toString()
        val newManufacture = itemManufacture.text.toString()
        val newPrice = itemPrice.text.toString().toDoubleOrNull() ?: 0.0
        val newQuantity = itemQuantity.text.toString().toIntOrNull() ?: 0

        stock.name = newName
        stock.manufacture = newManufacture
        stock.costPerItem = newPrice
        stock.quantity = newQuantity
        if (imageUrl != null){
            stock.itemPicture = imageUrl.toString()
        }


        val stockRepository = StockRepository.get()
        if (isNewStock) {
            // New stock, add it to the database
            Toast.makeText(requireContext(), "Stock saved", Toast.LENGTH_SHORT).show()
            stockRepository.addStock(stock)
        } else {
            // Existing stock, update it in the database
            Toast.makeText(requireContext(), "Stock Updated", Toast.LENGTH_SHORT).show()
            stockRepository.updateStock(stock)
        }

        loadMainActivity()
    }
    private fun deleteStock() {
        // Create a custom dialog
        val dialogView = layoutInflater.inflate(R.layout.delete_confirmation, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val yesButton = dialogView.findViewById<Button>(R.id.yesButton)
        val noButton = dialogView.findViewById<Button>(R.id.noButton)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        // Set click listeners for the Yes and No buttons
        yesButton.setOnClickListener {
            // User pressed "Yes", delete the item and close the dialog
            alertDialog.dismiss()
            val stockRepository = StockRepository.get()
            stockRepository.deleteStock(stock)
            Toast.makeText(requireContext(), "Stock Deleted", Toast.LENGTH_SHORT).show()
            loadMainActivity()
        }

        noButton.setOnClickListener {
            // User pressed "No", close the dialog
            alertDialog.dismiss()
        }
    }


    override fun onDateSelected(date: Date) {
        stock.restockDate = date
        TimePickerFragment.newInstance(stock.restockDate).apply {
            setTargetFragment(this@StockFragment, REQUEST_TIME)
            show(this@StockFragment.requireFragmentManager(), DIALOG_TIME)
        }

    }

    override fun onTimeSelected(time: Date) {
        stock.restockDate = time
        updateUI()
    }
    private fun updateQuantity(quantityChange: Int) {
        val currentQuantity = itemQuantity.text.toString().toIntOrNull() ?: 0
        val newQuantity = currentQuantity + quantityChange
        if (newQuantity >= 0) {
            itemQuantity.setText(newQuantity.toString())
        } else {
            // Display a toast or error message if quantity goes negative (optional)
            Toast.makeText(requireContext(), "Quantity cannot be negative!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun openImagePicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {


            val selectedImageUri = data?.data
            imageUrl = selectedImageUri.toString()
            updateImagePreview(imageUrl)
        }
    }
    private fun updateImagePreview(imageUrl: String?) {
        val imageView: ImageView = requireView().findViewById(R.id.itemImage)

        // Load the image using Glide
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView)
        } else {
            // If no image is selected, display a placeholder image
            imageView.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun loadItemImageFromUrl(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(itemImage)// Replace 'yourImageView' with the actual ImageView you want to load the image into
    }
    private fun loadMainActivity(){
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun allDataEntered(): Boolean {
        val requiredFields = listOf(
            Pair(itemName, "Item Name"),
            Pair(itemManufacture, "Item Manufacture"),
            Pair(itemPrice, "Item Price"),
            Pair(itemQuantity, "Item Quantity")
        )

        for (field in requiredFields) {
            if (field.first.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "${field.second} is required!",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        if (!isDateChanged){
            Toast.makeText(
                requireContext(),
                "Please choose a restock date",
                Toast.LENGTH_SHORT
            ).show()
            return false

        }


        val priceText = itemPrice.text.toString().trim()
        val price = priceText.toDoubleOrNull()
        if (price == null || price < 0) {
            Toast.makeText(
                requireContext(),
                "Item Price must be a non-negative number!",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Check if the price contains more than two decimal places
        val decimalIndex = priceText.indexOf('.')
        if (decimalIndex != -1 && priceText.length - decimalIndex - 1 > 2) {
            Toast.makeText(
                requireContext(),
                "Item Price should have up to two decimal places!",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        val quantity = itemQuantity.text.toString().toIntOrNull()
        if (quantity == null || quantity < 0) {
            Toast.makeText(
                requireContext(),
                "Item Quantity must be a non-negative integer!",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }






}