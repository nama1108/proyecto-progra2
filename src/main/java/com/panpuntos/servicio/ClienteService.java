package com.panpuntos.servicio;

import com.panpuntos.dao.ClienteDAO;
import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Cliente;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public void registrar(Cliente cliente) throws ValidacionException, SQLException {
        validar(cliente);
        if (clienteDAO.buscarPorIdentificacion(cliente.getIdentificacion()) != null) {
            throw new ValidacionException("Ya existe un cliente con la identificación " + cliente.getIdentificacion());
        }
        cliente.setPuntos(0);
        clienteDAO.insertar(cliente);
    }

    public void actualizar(Cliente cliente) throws ValidacionException, SQLException {
        validar(cliente);
        Cliente existente = clienteDAO.buscarPorIdentificacion(cliente.getIdentificacion());
        if (existente != null && existente.getIdCliente() != cliente.getIdCliente()) {
            throw new ValidacionException("Ya existe otro cliente con la identificación " + cliente.getIdentificacion());
        }
        clienteDAO.actualizar(cliente);
    }

    public void cambiarActivo(Cliente cliente, boolean activo) throws SQLException {
        clienteDAO.cambiarActivo(cliente.getIdCliente(), activo);
        cliente.setActivo(activo);
    }

    public Cliente buscarPorIdentificacion(String identificacion) throws SQLException {
        return clienteDAO.buscarPorIdentificacion(identificacion.trim());
    }

    public List<Cliente> buscar(String texto) throws SQLException {
        return clienteDAO.buscar(texto);
    }

    private void validar(Cliente cliente) throws ValidacionException {
        String identificacion = cliente.getIdentificacion();
        if (identificacion == null || identificacion.isBlank()) {
            throw new ValidacionException("La identificación es obligatoria");
        }
        if ("DPI".equals(cliente.getTipoIdentificacion()) && !identificacion.matches("\\d{13}")) {
            throw new ValidacionException("El DPI debe tener 13 dígitos");
        }
        if ("NIT".equals(cliente.getTipoIdentificacion()) && !identificacion.matches("\\d+-?[0-9Kk]")) {
            throw new ValidacionException("El NIT debe tener solo números y un dígito verificador (ej. 1234567-8)");
        }
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new ValidacionException("El nombre es obligatorio");
        }
        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank() && !cliente.getCorreo().contains("@")) {
            throw new ValidacionException("El correo no es válido");
        }
    }
}
