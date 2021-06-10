package com.mahi.evergreen.view.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentUpcyclingCreationBinding
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import com.mahi.evergreen.network.POST_IDEA_TYPE
import com.mahi.evergreen.network.POST_SERVICE_TYPE
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UpcyclingCreationFragment : BaseDialogFragment() {

    override var bottomNavigationViewVisibility: Int = View.GONE
    private var upcyclingType: Int? = 0
    private var categoryID: String? = null
    private lateinit var currentPhotoPath: String
    private var currentImageIndex = 0
    lateinit var photoFile: File
    private var imageList: Array<Bitmap?>? = arrayOf(null, null, null, null, null, null)
    private var placeholder: Drawable? = null
    private var imageUri: Uri? = null
    private var databaseService = DatabaseService()
    private var firebaseUser: FirebaseUser? = null
    private var storageRef: StorageReference? = null
    private var _binding: FragmentUpcyclingCreationBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpcyclingCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // prepare navigation toolbar
        binding.toolbarUpcyclingCreation.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarUpcyclingCreation.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }
        binding.bUpcyclingCreationClose.setOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }

        // retrieve extra arguments from intent
        upcyclingType = arguments?.getInt("upcyclingType")
        categoryID = arguments?.getString("categoryID")

        // depending on type of post, set navigation toolbar title
        if (upcyclingType == POST_SERVICE_TYPE){
            binding.toolbarUpcyclingCreation.title = resources.getString(R.string.upcycling_service_creation_toolbar_text)
        } else if(upcyclingType == POST_IDEA_TYPE) {
            binding.toolbarUpcyclingCreation.title = resources.getString(R.string.upcycling_idea_creation_toolbar_text)
            binding.etUpcyclingMinPrice.visibility = View.GONE
        }

        // establish Firebase Storage reference for future use
        storageRef = Firebase.storage.reference.child("Post Images")
        // Save Firebase user for future use
        firebaseUser = Firebase.auth.currentUser
        // prepare image placeholder for future use
        placeholder = this.context?.let { contextNotNull -> ContextCompat.getDrawable(contextNotNull, R.drawable.ic_baseline_add_box_24) }

        // determined whether customer device has camera
        if(hasCameraSupport()){
            listenersToloadImageWithCameraOrGallery()
        } else {// customer device does not have camera only offer gallery alternative
            listenersToloadImageUsingOnlyGallery()
        }
        // prepare listeners for image selection toolbar
        listenersForImageToolbarOptions()

        // prepare some needed values to create a post
        val type: Int = upcyclingType!!
        val publisherID: String = firebaseUser?.uid.toString()
        val category: MutableMap<String, Boolean> = HashMap()
        category["\"$categoryID\""] = true

        // Button for Post creation that start verification and loading process
        binding.bUpcyclingCreationBtn.setOnClickListener {
            // prepare some needed values to create a post
            var minPrice = binding.etUpcyclingMinPrice.text.toString()
            val title = binding.etUpcyclingTitle.text.toString()
            val description = binding.etUpcyclingDescription.text.toString()

            // verify that form provided was properly filled
            when {
                title == "" -> {
                    Toast.makeText(context, "introduce un título", Toast.LENGTH_LONG).show()
                }
                description == "" -> {
                    Toast.makeText(context, "introduce una descripción", Toast.LENGTH_LONG).show()
                }
                type == POST_SERVICE_TYPE && minPrice.isEmpty() -> {
                    Toast.makeText(context, "introduce un precio base", Toast.LENGTH_LONG).show()
                }
                imageList?.get(0) == null -> {
                    Toast.makeText(context, "introduce una imagen principal", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // if form provided was properly filled start post loading process
                    hideKeyboard()
                    // show loading placeholder layout to user while charging
                    binding.svUpcyclingCreation.visibility = View.GONE
                    binding.clUpcyclingCreationPlaceholder.visibility = View.VISIBLE
                    if(binding.clUpcyclingCreationPlaceholder.visibility == View.VISIBLE){
                        // if post type is POST_IDEA_TYPE we don't need price value
                        if (type == POST_IDEA_TYPE){
                            minPrice = "0.0"
                        }
                        // Start loading process
                        uploadPostToDatabase(
                            requireContext(),
                            type,
                            minPrice.toDouble(),
                            title,
                            publisherID,
                            category,
                            description
                        )
                    }
                }
            }
        }
    }

    private fun listenersForImageToolbarOptions() {
        // Image on position 0 => toolbar options = delete, rotate to the right, rotate to the left, full size view
        binding.deleteImage0.setOnClickListener {
            deleteImageSelected(0)
        }
        binding.rotateRightImage0.setOnClickListener {
            rotateImageSelected(0, 90f)
        }
        binding.rotateLeftImage0.setOnClickListener {
            rotateImageSelected(0, -90f)
        }
        binding.fullImage0.setOnClickListener {
            showFullImageSelected(0)
        }

        // Image on position 1 => toolbar options = delete, rotate to the right, rotate to the left, full size view
        binding.deleteImage1.setOnClickListener {
            deleteImageSelected(1)
        }
        binding.rotateRightImage1.setOnClickListener {
            rotateImageSelected(1, 90f)
        }
        binding.rotateLeftImage1.setOnClickListener {
            rotateImageSelected(1, -90f)
        }
        binding.fullImage1.setOnClickListener {
            showFullImageSelected(1)
        }

        // Image on position 2 => toolbar options = delete, rotate to the right, rotate to the left, full size view
        binding.deleteImage2.setOnClickListener {
            deleteImageSelected(2)
        }
        binding.rotateRightImage2.setOnClickListener {
            rotateImageSelected(2, 90f)
        }
        binding.rotateLeftImage2.setOnClickListener {
            rotateImageSelected(2, -90f)
        }
        binding.fullImage2.setOnClickListener {
            showFullImageSelected(2)
        }

        // Image on position 3 => toolbar options = delete, rotate to the right, rotate to the left, full size view
        binding.deleteImage3.setOnClickListener {
            deleteImageSelected(3)
        }
        binding.rotateRightImage3.setOnClickListener {
            rotateImageSelected(3, 90f)
        }
        binding.rotateLeftImage3.setOnClickListener {
            rotateImageSelected(3, -90f)
        }
        binding.fullImage3.setOnClickListener {
            showFullImageSelected(3)
        }

        // Image on position 4 => toolbar options = delete, rotate to the right, rotate to the left, full size view
        binding.deleteImage4.setOnClickListener {
            deleteImageSelected(4)
        }
        binding.rotateRightImage4.setOnClickListener {
            rotateImageSelected(4, 90f)
        }
        binding.rotateLeftImage4.setOnClickListener {
            rotateImageSelected(4, -90f)
        }
        binding.fullImage4.setOnClickListener {
            showFullImageSelected(4)
        }

        // Image on position 5 => toolbar options = delete, rotate to the right, rotate to the left, full size view
        binding.deleteImage5.setOnClickListener {
            deleteImageSelected(5)
        }
        binding.rotateRightImage5.setOnClickListener {
            rotateImageSelected(5, 90f)
        }
        binding.rotateLeftImage5.setOnClickListener {
            rotateImageSelected(5, -90f)
        }
        binding.fullImage5.setOnClickListener {
            showFullImageSelected(5)
        }

        // Close full size image btn
        binding.closeFullImage.setOnClickListener {
            closeFullSizeImage()
        }
    }

    private fun listenersToloadImageUsingOnlyGallery() {
        binding.image0.setOnClickListener {
            currentImageIndex = 0
            loadImageUsingOnlyGallery()
        }
        binding.image1.setOnClickListener {
            currentImageIndex = 1
            loadImageUsingOnlyGallery()
        }
        binding.image2.setOnClickListener {
            currentImageIndex = 2
            loadImageUsingOnlyGallery()
        }
        binding.image3.setOnClickListener {
            currentImageIndex = 3
            loadImageUsingOnlyGallery()
        }
        binding.image4.setOnClickListener {
            currentImageIndex = 4
            loadImageUsingOnlyGallery()
        }
        binding.image5.setOnClickListener {
            currentImageIndex = 5
            loadImageUsingOnlyGallery()
        }
    }

    private fun listenersToloadImageWithCameraOrGallery() {
        binding.image0.setOnClickListener {
            currentImageIndex = 0
            loadImageWithCameraOrGallery()
        }
        binding.image1.setOnClickListener {
            currentImageIndex = 1
            loadImageWithCameraOrGallery()
        }
        binding.image2.setOnClickListener {
            currentImageIndex = 2
            loadImageWithCameraOrGallery()
        }
        binding.image3.setOnClickListener {
            currentImageIndex = 3
            loadImageWithCameraOrGallery()
        }
        binding.image4.setOnClickListener {
            currentImageIndex = 4
            loadImageWithCameraOrGallery()
        }
        binding.image5.setOnClickListener {
            currentImageIndex = 5
            loadImageWithCameraOrGallery()
        }
    }

    /**
     * Starts the process to load a image from the gallery when camera is not available on the device
     */
    private fun loadImageUsingOnlyGallery() {
        checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, "Galería de fotos", REQUEST_GALLERY_ACCESS)
    }

    /**
     * Starts the process to load a image from the gallery or camera depending on the customer's selection
     */
    private fun loadImageWithCameraOrGallery() {
        val options = arrayOf<CharSequence>("Tomar una foto", "Seleccionar de galería")
        val builder : android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("¿Qué quieres hacer?")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // make sure the permissions needed are allowed
                    checkForPermissions(android.Manifest.permission.CAMERA, "Cámara", REQUEST_IMAGE_CAPTURE)
                }
                1 -> {
                    // make sure the permissions needed are allowed
                    checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, "Galería de fotos", REQUEST_GALLERY_ACCESS)
                }
            }
        }
        builder.show()
    }

    private fun closeFullSizeImage() {
        binding.imageFullSize.visibility = View.GONE
        binding.closeFullImage.visibility = View.GONE
    }

    /**
     * Show full size the image selected by the user on the image toolbar options
     */
    private fun showFullImageSelected(currentIndex: Int) {
        if(!imageList.isNullOrEmpty()){
            val imageSelected = imageList?.get(currentIndex)
            if (imageSelected != null){
                binding.imageFullSize.setImageBitmap(imageSelected)
                binding.imageFullSize.visibility = View.VISIBLE
                binding.closeFullImage.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Deletes the image selected by the user on the image toolbar options
     */
    private fun deleteImageSelected(currentIndex: Int) {
        imageList?.set(currentIndex, null)
        when(currentIndex){
            0 -> binding.image0.setImageDrawable(placeholder)
            1 -> binding.image1.setImageDrawable(placeholder)
            2 -> binding.image2.setImageDrawable(placeholder)
            3 -> binding.image3.setImageDrawable(placeholder)
            4 -> binding.image4.setImageDrawable(placeholder)
            5 -> binding.image5.setImageDrawable(placeholder)
        }
    }

    /**
     * Rotates the image selected by the user on the image toolbar options
     */
    private fun rotateImageSelected(currentIndex: Int, degrees: Float) {
        val imageSelected = imageList?.get(currentIndex)
        if (imageSelected != null) {
            imageList?.set(currentIndex, imageSelected.rotate(degrees))
            when(currentIndex){
                0 -> binding.image0.setImageBitmap(imageList?.get(currentIndex))
                1 -> binding.image1.setImageBitmap(imageList?.get(currentIndex))
                2 -> binding.image2.setImageBitmap(imageList?.get(currentIndex))
                3 -> binding.image3.setImageBitmap(imageList?.get(currentIndex))
                4 -> binding.image4.setImageBitmap(imageList?.get(currentIndex))
                5 -> binding.image5.setImageBitmap(imageList?.get(currentIndex))
            }
        }
    }

    /**
     * Handles the needed permissions for the camera or gallery and then calls intent for given activity
     */
    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        when {
            context?.let { ContextCompat.checkSelfPermission(it, permission) } == PackageManager.PERMISSION_GRANTED -> {
                // If the Permissions have been granted continue
                if (requestCode == REQUEST_IMAGE_CAPTURE){
                    dispatchTakePictureIntent()
                } else if (requestCode == REQUEST_GALLERY_ACCESS){
                    dispatchGalleryPictureIntent()
                }

            }
            // If the Permissions are not granted Request Permission
            shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

            else -> activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permission), requestCode) }
        }
    }

    /**
     * Takes care of the gallery intent to let the user pick a picture from the device internal storage
     */
    private fun dispatchGalleryPictureIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY_ACCESS)
    }

    /**
     * Shows dialog to explain to user why a given permission access is required for the app to work
     */
    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.apply {
            setMessage("Se requiere permiso para acceder a la $name para usar esta aplicación")
            setTitle("Permiso requerido")
            setPositiveButton("OK") { _, _ ->
                activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permission), requestCode) }
            }
        }
        val dialog = builder?.create()
        dialog?.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // If the Permissions have been granted continue
                    dispatchTakePictureIntent()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(context, "Debes permitir el acceso a la cámara para tomar fotos", Toast.LENGTH_LONG).show()
                }
                return
            }
            REQUEST_GALLERY_ACCESS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // If the Permissions have been granted continue
                    dispatchGalleryPictureIntent()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(context, "Debes permitir el acceso los archivos para elegir una imagen", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /**
     * Verifies if the given device has camera available
     */
    private fun hasCameraSupport(): Boolean {
        var hasSupport = false
        if(context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true){
            hasSupport = true
        }
        return hasSupport
    }

    /**
     * Takes care of the camera intent to let the user take a picture from the device camera
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.let { contextNotNull ->
                takePictureIntent.resolveActivity(contextNotNull.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.e("TakePictureIntentError", "ERROR ==> $ex")
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        imageUri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.mahi.evergreen.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    /**
     * Creates a temporal file from the image took from camera
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(currentPhotoPath)
            imageList?.set(currentImageIndex, takenImage)
            when(currentImageIndex){
                0 -> binding.image0.setImageBitmap(takenImage)
                1 -> binding.image1.setImageBitmap(takenImage)
                2 -> binding.image2.setImageBitmap(takenImage)
                3 -> binding.image3.setImageBitmap(takenImage)
                4 -> binding.image4.setImageBitmap(takenImage)
                5 -> binding.image5.setImageBitmap(takenImage)
            }

        } else if (requestCode == REQUEST_GALLERY_ACCESS && resultCode == RESULT_OK){
            data?.data?.let {
                val takenImage = BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(it))
                imageList?.set(currentImageIndex, takenImage)
                when(currentImageIndex){
                    0 -> binding.image0.setImageBitmap(takenImage)
                    1 -> binding.image1.setImageBitmap(takenImage)
                    2 -> binding.image2.setImageBitmap(takenImage)
                    3 -> binding.image3.setImageBitmap(takenImage)
                    4 -> binding.image4.setImageBitmap(takenImage)
                    5 -> binding.image5.setImageBitmap(takenImage)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * Rotates bitmap to a given degree and then returns modified bitmap
     */
    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /**
     * Uploads images to Firebase Storage and then Creates new Post JSON on Firebase Database
     */
    private fun uploadPostToDatabase(context: Context, type: Int, minPrice: Double?, title: String, publisherID: String, category: MutableMap<String, Boolean>, description: String) {
        // first we need to convert all bitmap images saved on application cache to Uri
        val imageUriList = getUriFromBitmaps(context, imageList)
        // prepare MutableMap value for images to load url on Firebase Database
        val images: MutableMap<String, String> = HashMap()

        // a counter to control image loading on Firebase Storage
        var counter = 1

        for (imgUri in imageUriList.withIndex()){

            // use Firebase Storage reference to save imageUri
            val fileRef = storageRef?.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask: StorageTask<*>
            if (fileRef != null) {
                uploadTask = fileRef.putFile(imgUri.value)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // if image is successfully load on Firebase Storage retrieve url from file
                        val downloadUri = task.result
                        val url = downloadUri.toString()
                        val index = "\"${counter}\""
                        // save url MutableMap value for images to load url on Firebase Database
                        images[index] = url

                        Log.d("debugImage", "counter => $counter")
                        Log.d("debugImage", "limit => ${imageUriList.size}")

                        // when all images all finally uploaded create Post on Firebase Database with all data needed
                        if (counter == imageUriList.size){
                            val coverURL: String = images["\"1\""].toString()
                            databaseService.writeNewPost(coverURL, type, minPrice, title, publisherID, category, description, images, object: Callback<Boolean> {
                                override fun onSuccess(result: Boolean?) {
                                    Log.i("FireBaseLogs", "Write post success")

                                    binding.UpcyclingCreationProgressBar.visibility = View.GONE
                                    binding.bUpcyclingCreationClose.visibility = View.VISIBLE
                                }

                                override fun onFailure(exception: Exception) {
                                    Log.w("FireBaseLogs", "Write failed")
                                }
                            })
                        }
                        counter++

                    } else {
                        counter++
                        Log.w("StorageLogs", "Save failed")
                    }
                }
            }
        }
    }

    /***
     * Returns a Collection of Uri objects from a Collection of bitmap objects
     *
     * This method saves a collection of bitmap objects on context.cacheDir as files
     * then retrieves the files and Creates a Uri from each file.
     */
    private fun getUriFromBitmaps(context: Context, imageList: Array<Bitmap?>?): ArrayList<Uri> {
        val uriList: ArrayList<Uri> = ArrayList()
        if (imageList != null) {
            var placeholderIndex = 1
            for (bitmap in imageList){
                if (bitmap != null) {

                    val file = File(context.cacheDir,"placeholderIndex$placeholderIndex") //Get Access to a local file.
                    file.delete() // Delete the File, just in Case, that there was still another File
                    file.createNewFile()
                    val fileOutputStream = file.outputStream()
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,70,byteArrayOutputStream)
                    val bytearray = byteArrayOutputStream.toByteArray()
                    fileOutputStream.write(bytearray)
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    byteArrayOutputStream.close()

                    val imageUri = file.toUri()
                    uriList.add(imageUri)

                    placeholderIndex++
                }
            }
        }
        return uriList
    }

    /***
     * Hides Keyboard on fragment
     */
    private fun BaseDialogFragment.hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        bottomNavigationViewVisibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 101
        const val REQUEST_GALLERY_ACCESS = 202
    }
}