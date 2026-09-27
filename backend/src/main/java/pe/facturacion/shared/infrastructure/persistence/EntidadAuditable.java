package pe.facturacion.shared.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Columnas de auditoría comunes: {@code creado_en}, {@code creado_por}, {@code actualizado_en}, {@code actualizado_por}. */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadAuditable {

	@CreatedDate
	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@CreatedBy
	@Column(name = "creado_por", length = 50, updatable = false)
	private String creadoPor;

	@LastModifiedDate
	@Column(name = "actualizado_en", nullable = false)
	private Instant actualizadoEn;

	@LastModifiedBy
	@Column(name = "actualizado_por", length = 50)
	private String actualizadoPor;

}
