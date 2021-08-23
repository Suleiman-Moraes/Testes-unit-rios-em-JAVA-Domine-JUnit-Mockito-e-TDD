package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {
	
	@Parameter
	public List<Filme> filmes;
	
	@Parameter(value = 1)
	public Double valorLocacao;
	
	@Parameter(value = 2)
	public String cenario;
	
	@InjectMocks
	private LocacaoService service;
	
	@Mock
	private SpcService spcService;
	
	@Mock
	private LocacaoDao dao;
	
	private static final Filme FILME_1 = FilmeBuilder.umFilme().agora();
	private static final Filme FILME_2 = FilmeBuilder.umFilme().agora();
	private static final Filme FILME_3 = FilmeBuilder.umFilme().agora();
	private static final Filme FILME_4 = FilmeBuilder.umFilme().agora();
	private static final Filme FILME_5 = FilmeBuilder.umFilme().agora();
	private static final Filme FILME_6 = FilmeBuilder.umFilme().agora();
	private static final Filme FILME_7 = FilmeBuilder.umFilme().agora();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Parameters(name = "{2}")
	public static Collection<Object[]> getParametros(){
		return Arrays.asList(new Object[][] {
			{Arrays.asList(FILME_1, FILME_2), 8.0, "2 Filmes: Sem desconto"},
			{Arrays.asList(FILME_1, FILME_2, FILME_3), 11.0, "3 Filmes: 25%"},
			{Arrays.asList(FILME_1, FILME_2, FILME_3, FILME_4), 13.0, "4 Filmes: 50%"},
			{Arrays.asList(FILME_1, FILME_2, FILME_3, FILME_4, FILME_5), 14.0, "5 Filmes: 75%"},
			{Arrays.asList(FILME_1, FILME_2, FILME_3, FILME_4, FILME_5, FILME_6), 14.0, "6 Filmes: 100%"},
			{Arrays.asList(FILME_1, FILME_2, FILME_3, FILME_4, FILME_5, FILME_6, FILME_7), 18.0, "7 Filmes: Sem desconto"},
		});
	}

	@Test
	public void deveCalcularValorLocacaoConsiderandoDescontos() throws Exception {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		// verificacao
		assertThat(locacao.getValor(), is(equalTo(valorLocacao)));
	}
}
