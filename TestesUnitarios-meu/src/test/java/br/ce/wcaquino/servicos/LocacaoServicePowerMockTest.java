package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersPropios.caiNaSegunda;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.matchers.MatchersPropios;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LocacaoService.class, DataUtils.class })
public class LocacaoServicePowerMockTest {

	@InjectMocks
	private LocacaoService service;

	@Mock
	private SpcService spcService;

	@Mock
	private LocacaoDao dao;

	@Mock
	private EmailService emailService;

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		service = PowerMockito.spy(service);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void deveAlugarFilme() throws Exception {
//		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().precoLocacao(5.0).agora());
		final Date data = DataUtils.obterData(28, 4, 2017);
		// acao
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(data);
		Locacao locacao = service.alugarFilme(usuario, filmes);

		// verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(locacao.getDataLocacao(), MatchersPropios.ehHoje());
		error.checkThat(locacao.getDataRetorno(), MatchersPropios.ehHojeComDiferencaDias(1));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), data), is(Boolean.TRUE));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)),
				is(Boolean.TRUE));
//		error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(Boolean.TRUE));
//		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)),
//				is(Boolean.TRUE));

	}

	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcessaoAoAlugarFilmeSemEstoque() throws Exception {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().agora());

		// acao
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void deveLancarExcessaoAoAlugarFilmeUsuarioVazio() throws Exception {
		// cenario
		final Usuario usuario = null;
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		try {
			// acao
			service.alugarFilme(usuario, filmes);
			fail("Deveria lançar uma LocadoraException");
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário vazio."));
		}
	}

	@Test
	public void deveLancarExcessaoAoAlugarFilmeVazio() throws Exception {
		expected.expect(LocadoraException.class);
		expected.expectMessage("Filme vazio.");
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = null;

		// acao
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(29, 4, 2017));

		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		// verificacao
//		assertThat(Boolean.TRUE, is(DataUtils.verificarDiaSemana(locacao.getDataRetorno(), Calendar.MONDAY)));
//		assertThat(locacao.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
//		assertThat(locacao.getDataRetorno(), caiEm(Calendar.MONDAY));
		assertThat(locacao.getDataRetorno(), caiNaSegunda());
		PowerMockito.verifyNew(Date.class, Mockito.times(2)).withNoArguments();
	}

	@Test
	public void naoDeveAlugarFilmeParaNegativadoSpc() throws Exception {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		Mockito.when(spcService.possuiNagativacao(Mockito.any(Usuario.class))).thenReturn(Boolean.TRUE);
		// acao
		try {
			service.alugarFilme(usuario, filmes);

			// verificacao

			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário negativado."));
		}
		Mockito.verify(spcService).possuiNagativacao(usuario);
	}

	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final Usuario usuarioDia = UsuarioBuilder.umUsuario().nome("Usuário em dias").agora();
		final Usuario usuario2 = UsuarioBuilder.umUsuario().nome("Usuário atrasado").agora();
		final List<Locacao> locacoes = Arrays.asList(LocacaoBuilder.umLocacaoAtrasado().comUsuario(usuario).agora(),
				LocacaoBuilder.umLocacao().comUsuario(usuarioDia).agora(),
				LocacaoBuilder.umLocacaoAtrasado().comUsuario(usuario2).agora(),
				LocacaoBuilder.umLocacaoAtrasado().comUsuario(usuario2).agora());
		Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

		// acao
		service.notificarAtrasos();

		// verificacao
		// Ver se foi chamado pelo menos duas vezes qualquer objeto do tipo Usuario
		Mockito.verify(emailService, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));

		Mockito.verify(emailService).notificarAtraso(usuario);
		// Ver se foi chamado ao menos uma única vez

		Mockito.verify(emailService, Mockito.atLeastOnce()).notificarAtraso(usuario2);

		// Famoso nunk nem vi
		Mockito.verify(emailService, Mockito.never()).notificarAtraso(usuarioDia);

		Mockito.verifyNoMoreInteractions(emailService);
	}

	@Test
	public void deveTratarErroNoSpc() throws Exception {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		Mockito.when(spcService.possuiNagativacao(usuario)).thenThrow(new Exception("Falha catastrófica."));

		// verificacao
		expected.expect(LocadoraException.class);
		expected.expectMessage("Problemas com SPC, tente novamente.");

		// acao
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void deveProrrogarUmaLocacao() throws Exception {
		// cenario
		final Locacao locacao = LocacaoBuilder.umLocacao().agora();

		// acao
		service.prorrogarLocacao(locacao, 3);

		// verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		error.checkThat(locacaoRetornada.getValor(), is(4.0 * 3));
		error.checkThat(locacaoRetornada.getDataLocacao(), MatchersPropios.ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), MatchersPropios.ehHojeComDiferencaDias(3));
	}
	
	@Test
	public void deveAlugarFilmeSemCalcularValor() throws Exception {
		// cenario
		final Usuario usuario = UsuarioBuilder.umUsuario().agora();
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		PowerMockito.doReturn(1.0).when(service, "calcularValorLocacao", filmes);
		
		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		// verificacao
		error.checkThat(locacao.getValor(), is(equalTo(1.0)));
		PowerMockito.verifyPrivate(service).invoke("calcularValorLocacao", filmes);
	}

	@Test
	public void deveCalcularValorLocacao() throws Exception {
		// cenario
		final List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		// acao
		Double valor = Whitebox.invokeMethod(service, "calcularValorLocacao", filmes);

		// verificacao
		error.checkThat(valor, is(equalTo(4.0)));
		PowerMockito.verifyPrivate(service).invoke("calcularValorLocacao", filmes);
	}
}
