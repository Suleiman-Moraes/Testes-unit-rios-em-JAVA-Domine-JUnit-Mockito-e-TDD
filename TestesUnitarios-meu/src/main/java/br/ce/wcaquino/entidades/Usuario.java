package br.ce.wcaquino.entidades;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Usuario {

	private String nome;
	
	public Usuario(String nome) {
		this.nome = nome;
	}
}