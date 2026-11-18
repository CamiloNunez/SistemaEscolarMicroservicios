package com.thales_mileto.login.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Microservicio de Login</title>

                    <style>
                        * {
                            box-sizing: border-box;
                            margin: 0;
                            padding: 0;
                        }

                        body {
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-family: Arial, sans-serif;
                            background: #f4f6f9;
                        }

                        .card {
                            background: white;
                            padding: 40px;
                            border-radius: 15px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.10);
                            text-align: center;
                            max-width: 500px;
                            width: 90%;
                        }

                        .status {
                            font-size: 50px;
                            margin-bottom: 15px;
                        }

                        h1 {
                            color: #1f2937;
                            margin-bottom: 10px;
                        }

                        p {
                            color: #6b7280;
                            margin: 8px 0;
                        }

                        .online {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 8px 18px;
                            background: #dcfce7;
                            color: #15803d;
                            border-radius: 20px;
                            font-weight: bold;
                        }
                    </style>
                </head>

                <body>
                    <div class="card">

                        <div class="status">🔐</div>

                        <h1>Microservicio de Login</h1>

                        <p>IEP Thales de Mileto</p>

                        <p>
                            Servicio encargado de la autenticación
                            y autorización de usuarios.
                        </p>

                        <div class="online">
                            ● Servicio activo
                        </div>

                    </div>
                </body>
                </html>
                """;
    }
}