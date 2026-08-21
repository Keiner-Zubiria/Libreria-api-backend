package com.sena.api_libreria.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String subirArchivo(MultipartFile archivo, String carpeta) {
        try {
            String nombreOriginal = archivo.getOriginalFilename();
            String publicId = carpeta + "/" + System.currentTimeMillis() + "_" + nombreOriginal;

            Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "auto"
            );

            Map<?, ?> result = cloudinary.uploader().upload(archivo.getBytes(), params);
            String url = (String) result.get("secure_url");
            log.info("Archivo subido a Cloudinary: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Error al subir archivo a Cloudinary: {}", e.getMessage(), e);
            return null;
        }
    }

    public void eliminarArchivo(String url) {
        try {
            String publicId = extraerPublicId(url);
            if (publicId != null && !publicId.isBlank()) {
                log.info("Eliminando archivo de Cloudinary: {}", publicId);
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            log.warn("No se pudo eliminar archivo de Cloudinary: {}", e.getMessage());
        }
    }

    private String extraerPublicId(String url) {
        if (url == null || url.isBlank()) return null;

        if (!url.startsWith("http")) return null;

        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx < 0) return null;

        String despues = url.substring(uploadIdx + 8);

        int slashIdx = despues.indexOf('/');
        if (slashIdx >= 0 && despues.startsWith("v") && despues.substring(1, slashIdx).matches("\\d+")) {
            despues = despues.substring(slashIdx + 1);
        }

        int puntoIdx = despues.lastIndexOf('.');
        if (puntoIdx >= 0) {
            despues = despues.substring(0, puntoIdx);
        }

        return despues;
    }
}
