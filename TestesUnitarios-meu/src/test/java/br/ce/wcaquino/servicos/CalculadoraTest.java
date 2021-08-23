package br.ce.wcaquino.servicos;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;

public class CalculadoraTest {
	
	private Calculadora calc;
	
	@Before
	public void setup() {
		calc = new Calculadora();
	}
	
	@Test
	public void deveSomarDoisValores() {
		//cenario
		final int a = 5;
		final int b = 3;
		
		//acao
		final int resultado = calc.somar(a, b);
		
		//verificar
		assertEquals(8, resultado);
	}
	
	@Test
	public void deveSubtrairDoisValores() {
		//cenario
		final int a = 8;
		final int b = 5;
		
		//acao
		final int resultado = calc.subtrair(a, b);
		
		//verificar
		assertEquals(3, resultado);
	}
	
	@Test
	public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
		//cenario
		final int a = 6;
		final int b = 3;
		
		//acao
		final int resultado = calc.dividir(a, b);
		
		//verificar
		assertEquals(2, resultado);
	}

	@Test(expected =  NaoPodeDividirPorZeroException.class)
	public void deveLancarExcessaoAoDividirPorZero() throws NaoPodeDividirPorZeroException {
		//cenario
		final int a = 6;
		final int b = 0;
		
		//acao
		calc.dividir(a, b);
	}
}
