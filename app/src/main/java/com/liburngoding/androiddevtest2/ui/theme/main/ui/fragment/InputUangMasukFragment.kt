package com.liburngoding.androiddevtest2.ui.theme.main.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.liburngoding.androiddevtest2.R
import com.liburngoding.androiddevtest2.databinding.FragmentInputUangMasukBinding
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction
import com.liburngoding.androiddevtest2.ui.theme.main.utilitas.GetRealPathFromURI
import com.liburngoding.androiddevtest2.ui.theme.main.ui.adapter.AdapterTransaction
import com.liburngoding.androiddevtest2.ui.theme.main.ui.bottomsheet.CustomBottomSheetDialogFragment
import com.liburngoding.androiddevtest2.ui.theme.main.ui.viewmodel.TransactionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class InputUangMasukFragment : Fragment(), AdapterTransaction.OnTransactionDeleteListener, AdapterTransaction.OnTransactionUpdateListener {
    private var _binding: FragmentInputUangMasukBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionViewModel : TransactionViewModel

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private var currentPermissionRequest: String? = null
    var getFilePhoto : String = ""
    var checkUpdate : Boolean = false
    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val ARG_TRANSACTION = "transaction"

        fun newInstance(transaction: Int): InputUangMasukFragment {
            val fragment = InputUangMasukFragment()
            val args = Bundle()
            args.putInt(ARG_TRANSACTION, transaction)
            fragment.arguments = args
            return fragment
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            when (currentPermissionRequest) {
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES -> openGallery()
                Manifest.permission.CAMERA -> startCamera()
            }
        } else {
            Toast.makeText(requireContext(), "Ijin diperlukan", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       _binding = FragmentInputUangMasukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        // Ambil data dari arguments
        val transactionId = arguments?.getInt(ARG_TRANSACTION)
        Log.d("faiz nazhir", transactionId.toString())
        if(transactionId != null){
            checkUpdate = true
            transactionViewModel.getTransactionById(transactionId!!.toInt()).observe(viewLifecycleOwner) { transaction ->
                transaction?.let {
                    binding.edSource.setText(transaction.source_data)
                    binding.edDestination.setText(transaction.destination_data)
                    binding.edAmount.setText(transaction.income_amount)
                    binding.edDescription.setText(transaction.description)

                    val savedCategory = transaction.category

                    val position = (binding.spinnerCategory.adapter as ArrayAdapter<String>).getPosition(savedCategory)


                    if (position >= 0) {
                        binding.spinnerCategory.setSelection(position)
                    }

                    val bitmap = BitmapFactory.decodeFile(transaction.files)

                    binding.imageResult.setImageBitmap(bitmap)
                }
            }
        }



        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
            val galleryPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (cameraPermissionGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Izin Kamera Diperlukan", Toast.LENGTH_SHORT).show()
            }

            if (galleryPermissionGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Izin Galeri Diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    binding.parentChangePhoto.visibility = View.VISIBLE
                    handleImage(imageUri)
                    val path = GetRealPathFromURI.getRealPath(requireContext(), imageUri)
                    if (path != null) {
                        getFilePhoto = path
                    }
                }
            }
        }
        cameraExecutor = Executors.newSingleThreadExecutor()


        binding.btnSave.setOnClickListener {
            if (checkUpdate == true){
                updateDatabase(transactionId!!)
            }else{
                insertDatabase()
            }

        }

        binding.btnMoreDetail.setOnClickListener {
            initializationBottomSheet()
        }
        binding.takePicture.setOnClickListener {
            takePhoto()
        }
        binding.btnOpenCameraGallery.setOnClickListener {
            showCustomDialog(requireContext())
        }

        binding.btnChangePhoto.setOnClickListener {
            showCustomDialog(requireContext())
        }
        binding.btnDeletePhoto.setOnClickListener {
            binding.imageResult.setImageBitmap(null)
            binding.parentChangePhoto.visibility = View.GONE
        }
        binding.icBackActivity.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

    }

    private fun updateDatabase(transactionId: Int){
        val destination = binding.edDestination.text.toString().trim()
        val source = binding.edSource.text.toString().trim()
        val amount = binding.edAmount.text.toString().trim()
        val description = binding.edDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        if (inputCheck(destination, source, amount)) {

            val transaction = Transaction(
                id = transactionId,
                destination_data = destination,
                source_data = source,
                income_amount = amount,
                description = description,
                category = category,
                files = getFilePhoto,
                created_at = System.currentTimeMillis()

            )
            transactionViewModel.updateTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction Update", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializationBottomSheet(){
        val bottomSheetFragment = CustomBottomSheetDialogFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }
    private fun insertDatabase(){
        val destination = binding.edDestination.text.toString().trim()
        val source = binding.edSource.text.toString().trim()
        val amount = binding.edAmount.text.toString().trim()
        val description = binding.edDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()

        if (inputCheck(destination, source, amount)) {
            // Create Transaction Object
            val transaction = Transaction(
                destination_data = destination,
                source_data = source,
                income_amount = amount,
                description = description,
                category = category,
                files = getFilePhoto,
                created_at = System.currentTimeMillis()

            )

            // Add Data to Database via ViewModel
            transactionViewModel.insert(transaction)
            Toast.makeText(requireContext(), "Transaction Added", Toast.LENGTH_SHORT).show()

            // Optionally navigate back or clear input fields
        } else {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inputCheck(destination: String, source: String, amount: String): Boolean {
        return !(TextUtils.isEmpty(destination) || TextUtils.isEmpty(source) || TextUtils.isEmpty(amount))
    }


    fun showCustomDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_camera_gallery, null)

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnGallery = dialogView.findViewById<ImageView>(R.id.btnGallery)
        val btnCamera = dialogView.findViewById<ImageView>(R.id.btnCamera)

        btnGallery.setOnClickListener {
            checkPermissionAndOpenGallery()
            dialog.dismiss()
        }
        btnCamera.setOnClickListener {
            checkPermissionsAndOpenCamera()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK)
        pickImageIntent.type = "image/*"
        pickImageIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        pickImageLauncher.launch(pickImageIntent)
    }

    private fun handleImage(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Tampilkan gambar di ImageView
        binding.imageResult.setImageBitmap(bitmap)
    }
    private fun checkPermissionsAndOpenCamera() {
        currentPermissionRequest = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(requireContext(), currentPermissionRequest!!) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(currentPermissionRequest!!)
        } else {
            startCamera()
        }
    }
    private fun checkPermissionAndOpenGallery() {
        currentPermissionRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), currentPermissionRequest!!) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(currentPermissionRequest!!)
        } else {
            openGallery()
        }
    }

    private fun startCamera() {
        binding.parentCamera.visibility = View.VISIBLE
        binding.viewFinder.visibility = View.VISIBLE
        binding.takePicture.visibility = View.VISIBLE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1080, 1920))
                .build()


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()


                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Gagal mengikat kamera", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {

        val photoFile = File(
            requireContext().externalMediaDirs.firstOrNull(),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )


        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Gagal mengambil gambar: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(), "Gambar disimpan: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                    getFilePhoto = photoFile.absolutePath
                    binding.parentCamera.visibility = View.GONE
                    binding.viewFinder.visibility = View.GONE
                    binding.takePicture.visibility = View.GONE
                    binding.parentChangePhoto.visibility = View.VISIBLE

                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    binding.imageResult.setImageBitmap(bitmap)
                }
            }
        )
    }

    override fun onDeleteTransaction(transactionId: Int) {
    }
    override fun onUpdateTransaction(transactionId: Int) {
    }
    override fun onViewPhoto(path: String) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown() // Bersihkan executor
        _binding = null
    }

}