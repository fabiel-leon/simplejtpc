/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.entities;

/**
 *
 * @author administrador
 */
public enum Category {

    pc(5), monitor(19), hdd(20), memorias(21), board(22), micro(23), chasis(24), fuente(25),
    backup(26), video_sonido(27), laptop(29), quemador(30), impresora(31), red(32), internet(33),
    accesorio(34), piezas(76), autos(13), animales(1), antiguedades(2), cd_dvd(4), electrodomesticos(8),
    tools(9), muebles(11), revistas(12), peliculas(14), belleza(15), ropa(16), joyas(35), gym(36), aire_acondicionado(37), tv(38), dvdplayer(39), equipo(40), piezas_movil(41), camara(42),
    instrumentosMusica(65), ferreteria(67), juegos(69), ipod(70), movil(72), comercio(73);
//revolico
//    pc(2), laptop(3), microprocesador(5), monitor(4), motherboard(6), memoria(7), disco_duro(8), chasis_fuente(9),
//    tarjeta_video(11), tarjeta_sonido_bocinas(12), quemador_cd_dvd(13), backup(14), impresora(15),
//    modem_wifi_red(16), webcam_microf_audifono(18), teclado_mouse(19), internet_email(216), cd_dvd(218),
//    celulares(31), mp3_mp4_ipod(32), dvd_vcd_dvr(33), tv(214), foto_video(74), aire_acondicionado(213),
//    consola_juegos(39), satelite(43), electrodomesticos(35), muebles_decoracion(36), cursos(83), informatica(71),
//    ropa_zapato_accesorios(37), mascotas(41), antiguedades(217), deportivos(219), peliculas(72), construccion(75), electronica(76), peluqueria_belleza(77),
//    restaurantes(78), decoracion(79), animacion_shows(80), joyero(81), gimnasio_masaje(220), vivienda(101), permuta(102),
//    alquiler_cubano(103), alquiler_extranjero(104), carros(121), motos(122), alquiler_autos(124), mecanico(123),
//    empleo(162);
    private final Integer value;

    private Category(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

}
