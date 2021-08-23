package br.ce.wcaquino.daos;

import java.util.List;

import br.ce.wcaquino.entidades.Locacao;

public interface LocacaoDao {

	void salvar(Locacao locacao);

	List<Locacao> obterLocacoesPendentes();
}
