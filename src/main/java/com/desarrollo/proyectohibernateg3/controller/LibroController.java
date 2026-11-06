package com.desarrollo.proyectohibernateg3.controller;

import javafx.scene.control.*;
import com.desarrollo.proyectohibernateg3.dao.LibroDAO;
import com.desarrollo.proyectohibernateg3.model.Libro;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public class LibroController {
    private final LibroDAO libroDAO = new LibroDAO();
    private Long idSeleccionado;

    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private TextField txtAnio;
    @FXML
    private TextField txtPrecio;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCargar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Libro> tablaLibros;
    @FXML
    private TableColumn<Libro, Long> colId;
    @FXML
    private TableColumn<Libro, String> colTitulo;
    @FXML
    private TableColumn<Libro, String> colAutor;
    @FXML
    private TableColumn<Libro, Integer> colAnio;
    @FXML
    private TableColumn<Libro, BigDecimal> colPrecio;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tablaLibros.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarLibro(newValue);
                    } else {
                        idSeleccionado = null;
                        actualizarBotones();
                    }
                }
        );

        actualizarBotones();
        cargarLibros();
    }

    private void seleccionarLibro(Libro libro) {
        idSeleccionado = libro.getId();
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtPrecio.setText(libro.getPrecio().toPlainString());
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnModificar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Libro obtenerLibroFormulario() {
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        if (titulo.isBlank() || autor.isBlank()
                || txtAnio.getText().isBlank() || txtPrecio.getText().isBlank()) {
            throw new IllegalArgumentException("Completa todos los campos.");
        }
        if (titulo.length() > 150 || autor.length() > 100) {
            throw new IllegalArgumentException("El título admite 150 caracteres y el autor 100.");
        }
        int anio = Integer.parseInt(txtAnio.getText().trim());
        BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
        if (anio < 1450 || anio > 2100 || precio.signum() < 0) {
            throw new IllegalArgumentException("El año debe estar entre 1450 y 2100 y el precio no puede ser negativo.");
        }

        return new Libro(idSeleccionado, titulo, autor, anio, precio);
    }

    @FXML
    private void guardarLibro() {
        try {
            Libro libro = obtenerLibroFormulario();
            libro.setId(null);
            libroDAO.guardar(libro);
            limpiarCampos();
            cargarLibros();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError(ex);
        }
    }
    @FXML
    private void modificarLibro() {
        try {
            if (idSeleccionado == null) {
                throw new IllegalArgumentException("Selecciona un libro para modificar.");
            }
            Libro libro = obtenerLibroFormulario();
            libro.setId(idSeleccionado);
            boolean modificado = libroDAO.modificar(libro);
            if (!modificado) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Libro no encontrado");
                alert.showAndWait();
            }

            limpiarCampos();
            cargarLibros();
        }catch (Exception ex){
            ex.printStackTrace();
            mostrarError(ex);
        }
    }
    @FXML
    private void eliminarLibro() {
        try {
            if (idSeleccionado == null) {
                throw new IllegalArgumentException("Selecciona un libro para eliminar.");
            }
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmacion de eliminacion de datos");
            confirmacion.setHeaderText("Eliminar libro");
            confirmacion.setContentText("¿Está seguro que desea eliminar el registro seleccionado?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if(resultado.isEmpty() || resultado.get() != ButtonType.OK){
                return;
            }

            boolean eliminado = libroDAO.eliminar(idSeleccionado);
            if (!eliminado) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Libro no encontrado");
                alert.showAndWait();

            }
            limpiarCampos();
            cargarLibros();
        }catch (Exception ex){
            ex.printStackTrace();
            mostrarError(ex);
        }
    }

    @FXML
    private void limpiarCampos() {
        tablaLibros.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();
        actualizarBotones();
    }

    @FXML
    private void cargarLibros() {
        try {
            List<Libro> libros = libroDAO.listar();
            tablaLibros.setItems(FXCollections.observableList(libros));
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError(ex);
        }
    }

    private void mostrarError(Exception ex) {
        String mensaje = ex instanceof NumberFormatException
                ? "Introduce un año entero y un precio numérico válido."
                : ex instanceof IllegalArgumentException
                    ? ex.getMessage()
                    : "No se pudo completar la operación. Revisa la conexión a la base de datos.";
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
