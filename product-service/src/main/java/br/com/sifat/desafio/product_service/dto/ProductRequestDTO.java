package br.com.sifat.desafio.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequestDTO {

    @Schema(description = "Nome do produto", example = "Teclado Mecânico")
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
    private String name;

    @Schema(description = "Preço do produto (formato decimal)", example = "199.99")
    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser positivo")
    private BigDecimal price;

    @Schema(description = "URL da imagem do produto", example = "http://example.com/image.png")
    @Size(max = 512, message = "A URL da imagem não pode exceder 512 caracteres")
    private String imageUrl;

    @Schema(description = "ID da Categoria do produto", example = "1")
    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

}
