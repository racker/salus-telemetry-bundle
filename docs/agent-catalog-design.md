# Agent Catalog and Installation Design

The goal of this design is to show how we will migrate Salus from using etcd for
managing the agent releases installs to instead use the same SQL approach as
monitor and resource management.

## Terminology

### Agent Release

The declaration of a specific version of an agent, such as telegraf, that is available
for Envoy's to download and install. An agent release has a version and requires two
tags: os and architecture. 

### Agent Catalog

As such, the collective set of agent releases is known as the agent catalog since it 
provides a catalog of all possible agent versions that could be selected for installation.

Only Salus admins can delcare agent releases in the agent catalog; however, end users
can list agent releases so that they can pick a release for installing. 

### Agent Install

## Schema

## Catalog management

## Installation propagation

