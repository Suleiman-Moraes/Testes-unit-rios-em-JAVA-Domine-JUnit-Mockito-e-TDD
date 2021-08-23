package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import lombok.Setter;

public class LocacaoService {

	@Setter
	private LocacaoDao dao;

	@Setter
	private SpcService spcService;

	@Setter
	private EmailService emailService;

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws Exception {
		if (filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Filme vazio.");
		}
		for (Filme filme : filmes) {
			if (filme.getEstoque() == null || filme.getEstoque() == 0) {
				throw new FilmeSemEstoqueException();
				// "Filme sem estoque."
			}
		}
		if (usuario == null) {
			throw new LocadoraException("Usuário vazio.");
		}

		boolean possuiNagativacao = false;
		try {
			possuiNagativacao = spcService.possuiNagativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas com SPC, tente novamente.");
		}

		if (possuiNagativacao) {
			throw new LocadoraException("Usuário negativado.");
		}

		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(obterData());
		locacao.setValor(calcularValorLocacao(filmes));

		// Entrega no dia seguinte
		Date dataEntrega = obterData();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);

		// Salvando a locacao...
		dao.salvar(locacao);

		return locacao;
	}

	protected Date obterData() {
		return new Date();
	}

	private Double calcularValorLocacao(List<Filme> filmes) {
		Double valorTotal = 0.0;
		for (int i = 0; i < filmes.size(); i++) {
			Filme filme = filmes.get(i);
			Double valor = filme.getPrecoLocacao();
			switch (i) {
			case 2:
				valor = valor * 0.75;
				break;
			case 3:
				valor = valor * 0.50;
				break;
			case 4:
				valor = valor * 0.25;
				break;
			case 5:
				valor = 0.0;
				break;
			}
			valorTotal += valor;
		}
		return valorTotal;
	}

	public void notificarAtrasos() {
		final List<Locacao> locacoes = dao.obterLocacoesPendentes();
		locacoes.forEach(locacao -> {
			// locacao.getDataRetorno() vem antes (hoje)
			if (locacao.getDataRetorno().before(obterData())) {
				emailService.notificarAtraso(locacao.getUsuario());
			}
		});
	}
	
	public void prorrogarLocacao(Locacao locacao, int dias) {
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(obterData());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		dao.salvar(novaLocacao);
	}
}