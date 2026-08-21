package com.sena.api_libreria.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String subirArchivo(MultipartFile archivo, String carpeta) throws IOException {
        String nombreOriginal = archivo.getOriginalFilename();
        String publicId = carpeta + "/" + System.currentTimeMillis() + "_" + nombreOriginal;

        Map<String, Object> params = ObjectUtils.asMap(
            "public_id", publicId,
            "resource_type", "auto"
        );

        Map<?, ?> result = cloudinary.uploader().upload(archivo.getBytes(), params);
        return (String) result.get("secure_url");
    }

    public String eliminarArchivo(String url) {
        try {
            String publicId = extraerPublicId(url);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
            return "Eliminado";
        } catch (IOException e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    private String extraerPublicId(String url) {
        if (url == null || url.isBlank()) return null;
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx < 0) return null;
        String despues = url.substring(uploadIdx + 8);
        int puntoIdx = despues.lastIndexOf('.');
        if (puntoIdx >= 0) {
            despues = despues.substring(0, puntoIdx);
        }
        return despues;
    }
}
