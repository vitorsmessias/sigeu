package br.edu.utfpr.dv.sigeu.jsfbeans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import br.edu.utfpr.dv.sigeu.config.Config;
import br.edu.utfpr.dv.sigeu.entities.CategoriaItemReserva;
import br.edu.utfpr.dv.sigeu.entities.ItemReserva;
import br.edu.utfpr.dv.sigeu.entities.PeriodoLetivo;
import br.edu.utfpr.dv.sigeu.entities.Pessoa;
import br.edu.utfpr.dv.sigeu.entities.Reserva;
import br.edu.utfpr.dv.sigeu.entities.TipoReserva;
import br.edu.utfpr.dv.sigeu.enumeration.RepeticaoReservaEnum;
import br.edu.utfpr.dv.sigeu.enumeration.StatusReserva;
import br.edu.utfpr.dv.sigeu.exception.ExisteReservaConcorrenteException;
import br.edu.utfpr.dv.sigeu.service.CategoriaItemReservaService;
import br.edu.utfpr.dv.sigeu.service.EmailService;
import br.edu.utfpr.dv.sigeu.service.ItemReservaService;
import br.edu.utfpr.dv.sigeu.service.PeriodoLetivoService;
import br.edu.utfpr.dv.sigeu.service.PessoaService;
import br.edu.utfpr.dv.sigeu.service.ReservaService;
import br.edu.utfpr.dv.sigeu.service.TipoReservaService;
import br.edu.utfpr.dv.sigeu.vo.ReservaVO;

import com.adamiworks.utils.StringUtils;

@ManagedBean(name = "reservaBean")
@ViewScoped
public class ReservaBean extends JavaBean {

	// @ManagedProperty(value = "#{loginBean}")
	// private LoginBean loginBean;

	private static final long serialVersionUID = 7141232111444710485L;

	// Campos do formulário
	private String campoCategoria;
	private String campoItem;
	private String campoRepete = RepeticaoReservaEnum.SEM_REPETICAO.getId();
	private Date campoDataFimRepete;
	private Date campoData;
	private Date campoHoraInicial;
	private Date campoHoraFinal;
	private String motivo;
	private String campoUsuario;
	private TipoReserva tipoReserva;
	private String emailNotificacao;
	//
	private Integer campoHoraI;
	private Integer campoMinutoI;
	private Integer campoHoraF;
	private Integer campoMinutoF;
	private Boolean campoImportadas = true;
	//

	//
	// Objetos de controle da regra de negócio
	private CategoriaItemReserva categoriaItemReserva;
	private ItemReserva itemReserva;
	private ItemReserva itemReservaGravacao;
	private List<ItemReserva> listaItemDisponivel;
	private List<Reserva> listaMinhasReservas;
	private List<Reserva> listaTodasReservas;
	private Integer showTab = 1;
	private Pessoa usuario;
	private List<Pessoa> listaUsuario;
	private List<TipoReserva> listaTipoReserva;
	private RepeticaoReservaEnum repeticaoReservaEnum;

	@ManagedProperty(value = "#{loginBean.pessoaLogin}")
	private Pessoa pessoaLogin;
	//
	// Objetos de controle dos campos de autocompletar
	private List<CategoriaItemReserva> listaCategoriaItemReserva;
	private List<String> categorias;
	private List<ItemReserva> listaItemReserva;

	// Atributos para cancelamento
	private List<ReservaVO> listaReservaVO;
	private String motivoCancelamento;

	public ReservaBean() {
		super();
		// System.out.println("----> ReservaBean CONSTRUCTOR");
		this.limpa(true, true);
	}

	@PostConstruct
	public void teste() {
		// System.out.println("ADMIN = " +
		// loginBean.getPessoaLogin().getAdmin());
	}

	public String reservar() {
		return "/restrito/reserva/Reserva.xhtml";
	}

	/**
	 * Lista categorias no método autocompletar do campoCategoria
	 * 
	 * @param query
	 * @return
	 */
	public List<String> selecionaCategoria(String query) {
		List<String> list = new ArrayList<String>();
		listaCategoriaItemReserva = null;

		try {
			listaCategoriaItemReserva = CategoriaItemReservaService.pesquisar(
					query, true);

			if (listaCategoriaItemReserva != null
					&& listaCategoriaItemReserva.size() > 0) {
				for (CategoriaItemReserva i : listaCategoriaItemReserva) {
					list.add(i.getNome());
				}
			} else {
				this.addInfoMessage("Selecionar",
						"Nenhuma categoria encontrada.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage("Selecionar",
					"Erro na pesquisa de categorias.");
			return list;
		}

		return list;
	}

	/**
	 * Lista Itens para o método autocompletar
	 * 
	 * @param query
	 * @return
	 */
	public List<String> selecionaItem(String query) {
		List<String> list = new ArrayList<String>();
		listaItemReserva = null;

		if (this.categoriaItemReserva == null) {
			this.addWarnMessage("Selecionar",
					"Selecione uma categoria antes de pesquisar o item de reserva.");
			return list;
		}

		try {
			listaItemReserva = ItemReservaService.pesquisar(
					categoriaItemReserva, query, true);

			if (listaItemReserva != null && listaItemReserva.size() > 0) {
				for (ItemReserva i : listaItemReserva) {
					list.add(i.getNome());
				}
			} else {
				this.addInfoMessage("Selecionar",
						"Nenhum item de reserva encontrado.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage("Selecionar", "Erro na pesquisa de Itens.");
			return list;
		}

		return list;
	}

	/**
	 * Lista categorias no método autocompletar do campoCategoria
	 * 
	 * @param query
	 * @return
	 */
	public List<String> selecionaUsuario(String query) {
		campoUsuario = null;
		List<String> list = new ArrayList<String>();
		listaUsuario = null;
		usuario = null;

		try {
			listaUsuario = PessoaService.pesquisar(query, true, 14);

			if (listaUsuario != null && listaUsuario.size() > 0) {
				for (Pessoa p : listaUsuario) {
					list.add(p.getNomeCompleto() + " (Mat:" + p.getMatricula()
							+ ")");
				}
			} else {
				this.addInfoMessage("Selecionar", "Nenhum usuário encontrado.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.addErrorMessage("Selecionar", "Erro na pesquisa de usuários.");
			return list;
		}

		return list;
	}

	/**
	 * Seleciona o objeto de controle Categoria
	 */
	public void defineCategoria() {
		categoriaItemReserva = null;

		for (CategoriaItemReserva i : listaCategoriaItemReserva) {
			if (campoCategoria.equals(i.getNome())) {
				categoriaItemReserva = i;
				break;
			}
		}

		// listaCategoriaItemReserva = null;
		campoCategoria = categoriaItemReserva.getNome();
	}

	/**
	 * Seleciona o objeto de controle Item
	 */
	public void defineItem() {
		itemReserva = null;

		for (ItemReserva i : listaItemReserva) {
			if (campoItem.equals(i.getNome())) {
				itemReserva = i;
				break;
			}
		}

		// listaItemReserva = null;
		campoItem = itemReserva.getNome();
	}

	/**
	 * Seleciona o objeto de controle Usuário
	 */
	public void defineUsuario() {
		usuario = null;

		for (Pessoa p : listaUsuario) {
			String match = p.getNomeCompleto() + " (Mat:" + p.getMatricula()
					+ ")";

			if (campoUsuario.equals(match)) {
				usuario = p;
				break;
			}
		}

		campoUsuario = usuario.getNomeCompleto();
		emailNotificacao = usuario.getEmail();
	}

	public void pesquisa() {
		/** Valida período letivo atual */
		try {
			Calendar cc = Calendar.getInstance();
			cc.set(Calendar.HOUR_OF_DAY, 00);
			cc.set(Calendar.MINUTE, 00);
			cc.set(Calendar.SECOND, 00);
			cc.set(Calendar.MILLISECOND, 00);

			Date hoje = cc.getTime();
			PeriodoLetivo pl = PeriodoLetivoService.encontreAtual(Config
					.getInstance().getCampus(), hoje);

			if (pl == null) {
				throw new Exception("Nenhum Período Letivo Cadastrado");
			}

			if (campoData.after(pl.getDataFim())) {
				addWarnMessage("Reservas",
						"Não são permitidas reservas para o final do semestre atual. Consulte o DERDI.");
			} else {

				categoriaItemReserva = null;

				if (campoItem == null || campoItem.trim().equals("")) {
					this.itemReserva = null;
				}

				repeticaoReservaEnum = RepeticaoReservaEnum
						.getEnum(campoRepete);

				if (!repeticaoReservaEnum
						.equals(RepeticaoReservaEnum.SEM_REPETICAO)) {
					if (campoDataFimRepete == null
							|| campoDataFimRepete.before(campoData)
							|| campoDataFimRepete.compareTo(campoData) == 0) {
						addWarnMessage("consulta",
								"A data limite de repetição deve ser maior que a data da reserva.");
						// EditableValueHolder evh = (EditableValueHolder)
						// FacesContext
						// .getCurrentInstance().getViewRoot()
						// .findComponent(":frmPesquisaReserva:dataRepete");
						// evh.setValid(false);
						return;
					}
				}

				for (CategoriaItemReserva c : listaCategoriaItemReserva) {
					if (c.getNome().equals(campoCategoria)) {
						categoriaItemReserva = c;
						break;
					}
				}

				if (campoData == null || categoriaItemReserva == null
						|| campoHoraInicial == null || campoHoraFinal == null) {
					this.addErrorMessage("Informações insuficientes",
							"Necessário informar: Categoria, Data e Horário para buscar reservas.");
				} else {

					if (campoHoraInicial.after(campoHoraFinal)
							|| campoHoraInicial.equals(campoHoraFinal)) {
						this.addErrorMessage("Horário inválido",
								"Hora inicial deve ser menor que hora final.");
					} else {
						try {
							// Preenche a lista de reservas do dia
							// this.listaReservaDia =
							// ReservaService.pesquisaReservasDoDia(campoData,
							// categoriaItemReserva, itemReserva);

							// Preenche a lista de itens disponíveis
							listaItemDisponivel = ReservaService
									.pesquisaItemReservaDisponivel(campoData,
											campoHoraInicial, campoHoraFinal,
											categoriaItemReserva, itemReserva);

							if (listaItemDisponivel == null
									|| listaItemDisponivel.size() == 0) {
								this.addWarnMessage("Item Disponível",
										"Nenhum item disponível para a data e horário informados.");
							}

							// Preenche lista das minhas reservas
							this.listaMinhasReservas = ReservaService
									.pesquisaReservasEfetivadasDoUsuario(
											pessoaLogin, campoData,
											categoriaItemReserva, itemReserva,
											campoImportadas);

							// Rola entre a lista de itens disponíveis para
							// checar
							// se
							// realmente está disponível com o repeteco
							if (!repeticaoReservaEnum
									.equals(RepeticaoReservaEnum.SEM_REPETICAO)) {
								listaItemDisponivel = ReservaService
										.removeItensNaoDisponiveisParaReservaRecorrente(
												campoData, campoHoraInicial,
												campoHoraFinal,
												repeticaoReservaEnum,
												campoDataFimRepete,
												listaItemDisponivel);
							}

						} catch (Exception e) {
							addErrorMessage("Pesquisa", "Pesquisa falhou.");
							addErrorMessage("Pesquisa", e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e1) {
			handleException(e1);
		}
	}

	/**
	 * Pesquisa da tela de administração de reservas
	 * 
	 */
	public void pesquisaAdmin() {
		categoriaItemReserva = null;

		for (CategoriaItemReserva c : listaCategoriaItemReserva) {
			if (c.getNome().equals(campoCategoria)) {
				categoriaItemReserva = c;
				break;
			}
		}

		if (campoData == null || categoriaItemReserva == null
				|| campoHoraInicial == null || campoHoraFinal == null) {
			this.addErrorMessage("Informações insuficientes",
					"Necessário informar: Categoria, Data e Horário para buscar reservas.");
		} else {

			if (campoHoraInicial.after(campoHoraFinal)
					|| campoHoraInicial.equals(campoHoraFinal)) {
				this.addErrorMessage("Horário inválido",
						"Hora inicial deve ser menor que hora final.");
			} else {
				// Preenche a lista de todas as reservas conforme filtros
				try {
					this.listaTodasReservas = ReservaService
							.pesquisaReservasEfetivadas(campoData,
									campoHoraInicial, campoHoraFinal,
									categoriaItemReserva, itemReserva);
				} catch (Exception e) {
					addErrorMessage("Pesquisa", "A pequisa falhou.");
					addErrorMessage("Pesquisa", e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	public void reserva(ItemReserva i) {
		this.itemReservaGravacao = i;
		this.showTab = 2;
	}

	/**
	 * Grava uma reserva no banco de dados
	 * 
	 */
	public void gravaReserva() {
		Reserva reserva = new Reserva();
		reserva.setData(campoData);
		reserva.setHoraFim(campoHoraFinal);
		reserva.setHoraInicio(campoHoraInicial);
		reserva.setIdCampus(Config.getInstance().getCampus());
		reserva.setIdPessoa(pessoaLogin);
		reserva.setIdItemReserva(itemReservaGravacao);
		reserva.setMotivo(motivo);

		if (Config.getInstance().getPessoaLogin().getAdmin()) {
			if (usuario == null) {
				/** Quando o usuário informado não existe */

				// addWarnMessage("Usuário",
				// "Informe o usuário da reserva (quem irá utilizar).");
				reserva.setIdUsuario(pessoaLogin);
				reserva.setNomeUsuario(campoUsuario);
			} else {
				reserva.setIdUsuario(usuario);
				reserva.setNomeUsuario(usuario.getNomeCompleto());
			}

			if (emailNotificacao == null
					|| emailNotificacao.trim().length() <= 0
					|| emailNotificacao.indexOf("@") <= 0) {
				addWarnMessage("Email",
						"Informe um endereço de e-mail válido para notificação.");
				return;
			}
			reserva.setEmailNotificacao(emailNotificacao);
		} else {
			reserva.setIdUsuario(pessoaLogin);
			reserva.setNomeUsuario(pessoaLogin.getNomeCompleto());
			reserva.setEmailNotificacao(pessoaLogin.getEmail());
		}

		reserva.setIdTipoReserva(tipoReserva);
		reserva.setRotulo(StringUtils.left(tipoReserva.getDescricao().trim(),
				32));

		if (repeticaoReservaEnum.equals(RepeticaoReservaEnum.SEM_REPETICAO)) {
			gravaReservaNormal(reserva);
		} else if (repeticaoReservaEnum.equals(RepeticaoReservaEnum.SEMANAL)) {
			gravaReservaSemanal(reserva);
		}

		// Refaz a pesquisa após a gravação
		this.pesquisa();
	}

	/**
	 * @param reserva
	 */
	private void gravaReservaNormal(Reserva reserva) {
		try {
			boolean existeConcorrente = ReservaService
					.existeConcorrente(reserva);

			if (existeConcorrente) {
				addWarnMessage(
						"Gravar",
						"Já foi feita uma reserva para este recurso na data informada que conflita com o horário desejado. Verifique!");
			} else {
				try {
					ReservaService.criar(reserva);

					StatusReserva statusReserva = null;

					// Verifica se o status da reserva foi alterado durante a
					// gravação
					statusReserva = StatusReserva.getFromStatus(reserva
							.getStatus());

					switch (statusReserva) {
					case PENDENTE:
						addWarnMessage(
								"Reserva",
								"Pré-Reserva de "
										+ itemReservaGravacao.getNome()
										+ " gravada com sucesso. Aguarde a confirmação da reserva por e-mail que será feita pelo responsável.");
						break;

					case EFETIVADA:
						// Envia e-mail de confirmação
						EmailService.enviaEmailConfirmacao(reserva);

						addInfoMessage("Reserva", "Reserva de "
								+ itemReservaGravacao.getNome()
								+ " realizada com sucesso!");
						break;

					case CANCELADA:
						addErrorMessage("Reserva",
								"A reserva foi cancelada! Informe o administrador do sistema!");
						break;
					default:
						break;
					}

					// Limpa todos os campos de listas
					this.limpa(true, false);

					this.showTab = 1;

				} catch (Exception e) {
					addWarnMessage("Gravar",
							"Houve um erro ao tentar gravar a reserva. Tente novamente.");
					addErrorMessage("Gravar", e.getMessage());
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			addErrorMessage("Gravar",
					"Erro ao buscar reservas previamente realizadas!");
			e.printStackTrace();
		}
	}

	/**
	 * @param reserva
	 */
	private void gravaReservaSemanal(Reserva reserva) {
		try {
			List<Reserva> lista = ReservaService.criarRecorrente(reserva,
					repeticaoReservaEnum, campoDataFimRepete);

			StatusReserva statusReserva = null;

			// Verifica se o status da reserva foi alterado durante a
			// gravação
			statusReserva = StatusReserva.getFromStatus(reserva.getStatus());

			switch (statusReserva) {
			case PENDENTE:
				addWarnMessage(
						"Reserva",
						"Pré-Reserva de "
								+ itemReservaGravacao.getNome()
								+ " gravada com sucesso. Aguarde a confirmação da reserva por e-mail que será feita pelo responsável.");
				break;
			case EFETIVADA:
				// Envia e-mail de confirmação das reservas
				EmailService.enviaEmailConfirmacao(lista);

				addInfoMessage("Reserva",
						"Reserva de " + itemReservaGravacao.getNome()
								+ " realizada com sucesso!");
				break;

			case CANCELADA:
				addErrorMessage("Reserva",
						"A reserva foi cancelada! Informe o administrador do sistema!");
				break;

			default:
				break;
			}

			// Limpa todos os campos de listas
			this.limpa(true, false);

			this.showTab = 1;

		} catch (ExisteReservaConcorrenteException e) {
			String msg = "Há uma reserva concorrente para as datas subsequentes. Possivelmente foi gravada por outro usuário após a pesquisa. Refaça a pesquisa e tente novamente.";
			addWarnMessage("Gravar", msg);
			addErrorMessage("Gravar", e.getMessage());
		} catch (Exception e) {
			addWarnMessage("Gravar",
					"Houve um erro ao tentar gravar a reserva. Tente novamente.");
			addErrorMessage("Gravar", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Cancela a gravação de reserva e volta à tela de pesquisa
	 */
	public void cancelaUi() {
		this.showTab = 1;
		this.limpa(false, false);
	}

	/**
	 * Limpa os campos visuais
	 * 
	 * @param listas
	 */
	private void limpa(boolean listas, boolean filtros) {
		// Limpa item de reserva de gravação
		this.itemReservaGravacao = null;

		if (filtros) {
			Calendar hi = Calendar.getInstance();
			Calendar hf = Calendar.getInstance();

			hi.set(Calendar.HOUR_OF_DAY, 8);
			hi.set(Calendar.MINUTE, 0);

			hf.set(Calendar.HOUR_OF_DAY, 9);
			hf.set(Calendar.MINUTE, 0);

			campoHoraInicial = hi.getTime();
			campoHoraFinal = hf.getTime();
			campoData = Calendar.getInstance().getTime();
			campoCategoria = null;
			categoriaItemReserva = null;
			campoItem = null;
			itemReserva = null;
			tipoReserva = null;
			emailNotificacao = null;
			campoRepete = "N";
			campoDataFimRepete = null;
		}

		if (listas) {
			this.listaItemDisponivel = null;
			// this.listaReservaDia = null;
			this.listaMinhasReservas = null;
			this.listaTipoReserva = null;

			try {
				listaCategoriaItemReserva = CategoriaItemReservaService
						.pesquisar(null, true);
				listaTipoReserva = TipoReservaService.pesquisar(null, true);

				// System.out.println("Lista de Período Letivo: " +
				// listaPeriodoLetivo.size());

				this.categorias = new ArrayList<String>();
				for (CategoriaItemReserva c : listaCategoriaItemReserva) {
					categorias.add(c.getNome());
				}
			} catch (Exception e) {
				this.addErrorMessage("Categoria", "Erro ao buscar categorias!");
				e.printStackTrace();
			}
		}
	}

	public void cancelaReserva(Reserva r) {
		// Map<String, Object> options = new HashMap<String, Object>();
		// Map<String, List<String>> args = new HashMap<String, List<String>>();
		// List<String> transCode = new ArrayList<String>();
		// options.put("modal", true);
		// options.put("resizable", false);
		// // options.put("contentHeight", 500);
		// options.put("contentWidth", 900);
		//
		// transCode.add(r.getIdTransacao().getIdTransacao().toString());
		// args.put("trans", transCode);
		//
		// System.out.println("Passo 1");
		// RequestContext.getCurrentInstance().openDialog("CancelaReserva",
		// options, args);
		// System.out.println("Passo 2");
		listaReservaVO = ReservaService
				.listaReservaPorTransacao(Config.getInstance().getCampus(), r
						.getIdTransacao().getIdTransacao());
		this.showTab = 3;
	}

	/**
	 * Exclui todas as reservas marcadas
	 */
	public void cancelaReservas() {
		if (this.motivoCancelamento == null
				|| this.motivoCancelamento.trim().length() == 0) {
			this.addWarnMessage("Cancelamento",
					"Motivo do cancelamento não preenchido!");
		} else {
			if (listaReservaVO != null) {
				List<Reserva> listExcluir = new ArrayList<Reserva>();

				for (ReservaVO vo : listaReservaVO) {
					if (vo.isExcluir()) {
						try {
							Reserva r = ReservaService.pesquisaReservaPorId(vo
									.getIdReserva());
							r.setStatus(StatusReserva.CANCELADA.getStatus());
							listExcluir.add(r);
						} catch (Exception e) {
							addErrorMessage(
									"Erro",
									"Erro ao tentar carregar reserva "
											+ vo.getIdReserva() + " de "
											+ vo.getDataReserva());
						}
					}
				}

				if (listExcluir.size() == 0) {
					addWarnMessage("Reserva",
							"Selecione ao menos uma reserva para cancelar.");
					return;
				}

				try {
					EmailService.enviaEmailCancelamento(listExcluir,
							motivoCancelamento);
				} catch (Exception e) {
					addErrorMessage("Erro",
							"Erro ao tentar criar e-mail de exclusão de reserva.");
					e.printStackTrace();
				}

				for (Reserva r : listExcluir) {
					try {
						// ReservaService.excluir(r);
						ReservaService.cancelaReserva(r, motivoCancelamento);
					} catch (Exception e) {
						addErrorMessage(
								"Erro",
								"Erro ao tentar excluir reserva "
										+ r.getIdReserva());
						e.printStackTrace();
					}
				}

				this.showTab = 1;
				this.motivoCancelamento = "";

				// Refaz pesquisa
				pesquisa();

				addInfoMessage(
						"Reserva",
						"Reservas canceladas com sucesso! A confirmação será enviada por e-mail em instantes.");
			}
		}
	}

	/**
	 * Exclui todas as reservas selecionadas
	 */
	public void cancelaReservasTodas() {
		for (ReservaVO vo : listaReservaVO) {
			vo.setExcluir(true);
		}

		this.cancelaReservas();
	}

	/**********************************************************************************/

	public String getCampoCategoria() {
		return campoCategoria;
	}

	public List<Reserva> getListaTodasReservas() {
		return listaTodasReservas;
	}

	public void setListaTodasReservas(List<Reserva> listaTodasReservas) {
		this.listaTodasReservas = listaTodasReservas;
	}

	public void setCampoCategoria(String campoCategoria) {
		this.campoCategoria = campoCategoria;
	}

	public String getCampoItem() {
		return campoItem;
	}

	public void setCampoItem(String campoItem) {
		this.campoItem = campoItem;
	}

	public Date getCampoData() {
		return campoData;
	}

	public void setCampoData(Date campoData) {
		this.campoData = campoData;
	}

	public Date getCampoHoraInicial() {
		return campoHoraInicial;
	}

	public void setCampoHoraInicial(Date campoHoraInicial) {
		this.campoHoraInicial = campoHoraInicial;
	}

	public Date getCampoHoraFinal() {
		return campoHoraFinal;
	}

	public void setCampoHoraFinal(Date campoHoraFinal) {
		this.campoHoraFinal = campoHoraFinal;
	}

	public CategoriaItemReserva getCategoriaItemReserva() {
		return categoriaItemReserva;
	}

	public void setCategoriaItemReserva(
			CategoriaItemReserva categoriaItemReserva) {
		this.categoriaItemReserva = categoriaItemReserva;
	}

	public ItemReserva getItemReserva() {
		return itemReserva;
	}

	public void setItemReserva(ItemReserva itemReserva) {
		this.itemReserva = itemReserva;
	}

	public List<CategoriaItemReserva> getListaCategoriaItemReserva() {
		return listaCategoriaItemReserva;
	}

	public void setListaCategoriaItemReserva(
			List<CategoriaItemReserva> listaCategoriaItemReserva) {
		this.listaCategoriaItemReserva = listaCategoriaItemReserva;
	}

	public List<ItemReserva> getListaItemReserva() {
		return listaItemReserva;
	}

	public void setListaItemReserva(List<ItemReserva> listaItemReserva) {
		this.listaItemReserva = listaItemReserva;
	}

	public List<ItemReserva> getListaItemDisponivel() {
		return listaItemDisponivel;
	}

	public void setListaItemDisponivel(List<ItemReserva> listaItemDisponivel) {
		this.listaItemDisponivel = listaItemDisponivel;
	}

	public Pessoa getPessoaLogin() {
		return pessoaLogin;
	}

	public void setPessoaLogin(Pessoa pessoaLogin) {
		this.pessoaLogin = pessoaLogin;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public ItemReserva getItemReservaGravacao() {
		return itemReservaGravacao;
	}

	public void setItemReservaGravacao(ItemReserva itemReservaGravacao) {
		this.itemReservaGravacao = itemReservaGravacao;
	}

	public List<String> getCategorias() {
		return categorias;
	}

	public void setCategorias(List<String> categorias) {
		this.categorias = categorias;
	}

	public Integer getShowTab() {
		return showTab;
	}

	public void setShowTab(Integer showTab) {
		this.showTab = showTab;
	}

	public List<Reserva> getListaMinhasReservas() {
		return listaMinhasReservas;
	}

	public void setListaMinhasReservas(List<Reserva> listaMinhasReservas) {
		this.listaMinhasReservas = listaMinhasReservas;
	}

	public String getCampoUsuario() {
		return campoUsuario;
	}

	public void setCampoUsuario(String campoUsuario) {
		this.campoUsuario = campoUsuario;
	}

	public Pessoa getUsuario() {
		return usuario;
	}

	public void setUsuario(Pessoa usuario) {
		this.usuario = usuario;
	}

	public List<Pessoa> getListaUsuario() {
		return listaUsuario;
	}

	public void setListaUsuario(List<Pessoa> listaUsuario) {
		this.listaUsuario = listaUsuario;
	}

	public TipoReserva getTipoReserva() {
		return tipoReserva;
	}

	public void setTipoReserva(TipoReserva tipoReserva) {
		this.tipoReserva = tipoReserva;
	}

	public List<TipoReserva> getListaTipoReserva() {
		return listaTipoReserva;
	}

	public void setListaTipoReserva(List<TipoReserva> listaTipoReserva) {
		this.listaTipoReserva = listaTipoReserva;
	}

	public String getEmailNotificacao() {
		return emailNotificacao;
	}

	public void setEmailNotificacao(String emailNotificacao) {
		this.emailNotificacao = emailNotificacao;
	}

	public Integer getCampoHoraI() {
		return campoHoraI;
	}

	public void setCampoHoraI(Integer campoHoraI) {
		this.campoHoraI = campoHoraI;
	}

	public Integer getCampoMinutoI() {
		return campoMinutoI;
	}

	public void setCampoMinutoI(Integer campoMinutoI) {
		this.campoMinutoI = campoMinutoI;
	}

	public Integer getCampoHoraF() {
		return campoHoraF;
	}

	public void setCampoHoraF(Integer campoHoraF) {
		this.campoHoraF = campoHoraF;
	}

	public Integer getCampoMinutoF() {
		return campoMinutoF;
	}

	public void setCampoMinutoF(Integer campoMinutoF) {
		this.campoMinutoF = campoMinutoF;
	}

	public String getCampoRepete() {
		return campoRepete;
	}

	public void setCampoRepete(String campoRepete) {
		this.campoRepete = campoRepete;
	}

	public Date getCampoDataFimRepete() {
		return campoDataFimRepete;
	}

	public void setCampoDataFimRepete(Date campoDataFimRepete) {
		this.campoDataFimRepete = campoDataFimRepete;
	}

	public RepeticaoReservaEnum getRepeticaoReservaEnum() {
		return repeticaoReservaEnum;
	}

	public void setRepeticaoReservaEnum(
			RepeticaoReservaEnum repeticaoReservaEnum) {
		this.repeticaoReservaEnum = repeticaoReservaEnum;
	}

	public List<ReservaVO> getListaReservaVO() {
		return listaReservaVO;
	}

	public void setListaReservaVO(List<ReservaVO> listaReservaVO) {
		this.listaReservaVO = listaReservaVO;
	}

	public String getMotivoCancelamento() {
		return motivoCancelamento;
	}

	public void setMotivoCancelamento(String motivoCancelamento) {
		this.motivoCancelamento = motivoCancelamento;
	}

	public Boolean getCampoImportadas() {
		return campoImportadas;
	}

	public void setCampoImportadas(Boolean campoImportadas) {
		this.campoImportadas = campoImportadas;
	}

	// public LoginBean getLoginBean() {
	// return loginBean;
	// }
	//
	// public void setLoginBean(LoginBean loginBean) {
	// this.loginBean = loginBean;
	// }

}
