package io.github.zanella.nomad.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xebia.jacksonlombok.JacksonLombokAnnotationIntrospector;

import io.github.zanella.nomad.v1.agent.AgentApi;
import io.github.zanella.nomad.v1.allocations.AllocationApi;
import io.github.zanella.nomad.v1.allocations.AllocationsApi;
import io.github.zanella.nomad.v1.client.ClientApi;
import io.github.zanella.nomad.v1.evaluations.EvaluationApi;
import io.github.zanella.nomad.v1.evaluations.EvaluationsApi;
import io.github.zanella.nomad.v1.jobs.JobApi;
import io.github.zanella.nomad.v1.jobs.JobsApi;
import io.github.zanella.nomad.v1.nodes.NodeApi;
import io.github.zanella.nomad.v1.nodes.NodesApi;
import io.github.zanella.nomad.v1.regions.RegionsApi;
import io.github.zanella.nomad.v1.status.StatusApi;

import lombok.Getter;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public final class V1Client {
    @Getter
    private final String agentAddress;

    public final StatusApi status;
    public final RegionsApi regions;

    public final NodesApi nodes;
    public final NodeApi node;

    public final JobsApi jobs;
    public final JobApi job;

    public final AllocationsApi allocations;
    public final AllocationApi allocation;

    public final EvaluationsApi evaluations;
    public final EvaluationApi evaluation;

    public final AgentApi agent;

    public final ClientApi client;

    public V1Client(String agentHost, int agentPort) {
        this.agentAddress = agentHost + ":" + agentPort;

        final ObjectMapper objectMapper = customObjectMapper();

        final Feign.Builder feignBuilder = Feign.builder()
                .decoder(new JacksonDecoder(objectMapper))
                .encoder(new JacksonEncoder(objectMapper))
                .logger(new Logger.ErrorLogger());
                //.logLevel(Logger.Level.FULL)

        this.status = feignBuilder.target(StatusApi.class, agentAddress);

        this.regions = feignBuilder.target(RegionsApi.class, agentAddress);

        this.nodes = feignBuilder.target(NodesApi.class, agentAddress);

        this.node = feignBuilder.target(NodeApi.class, agentAddress);

        this.jobs = feignBuilder.target(JobsApi.class, agentAddress);

        this.job = feignBuilder.target(JobApi.class, agentAddress);

        this.allocations = feignBuilder.target(AllocationsApi.class, agentAddress);

        this.allocation = feignBuilder.target(AllocationApi.class, agentAddress);

        this.evaluations = feignBuilder.target(EvaluationsApi.class, agentAddress);

        this.evaluation = feignBuilder.target(EvaluationApi.class, agentAddress);

        this.agent = feignBuilder.target(AgentApi.class, agentAddress);

        this.client = feignBuilder.target(ClientApi.class, agentAddress);
    }

    protected ObjectMapper customObjectMapper() {
        return new ObjectMapper()
                .setAnnotationIntrospector(new JacksonLombokAnnotationIntrospector())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.WRAP_ROOT_VALUE, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
